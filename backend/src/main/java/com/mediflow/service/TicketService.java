package com.mediflow.service;

import com.mediflow.entity.MedicalService;
import com.mediflow.entity.Ticket;
import com.mediflow.repository.MedicalServiceRepository;
import com.mediflow.repository.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final PriorityService priorityService; // Ton moteur IA
    private final MedicalServiceRepository medicalServiceRepository;
    private final com.mediflow.repository.DoctorRepository doctorRepository; // Injected
    private final EmailService emailService;

    public TicketService(TicketRepository ticketRepository,
                         PriorityService priorityService,
                         MedicalServiceRepository medicalServiceRepository,
                         com.mediflow.repository.DoctorRepository doctorRepository,
                         EmailService emailService) { 
        this.ticketRepository = ticketRepository;
        this.priorityService = priorityService;
        this.medicalServiceRepository = medicalServiceRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService; 
    }

    public List<Ticket> getPriorityQueue() {
        // 1. On récupère tous les patients au statut WAITING dans la clinique
        List<Ticket> waitingTickets = ticketRepository.findByStatus("WAITING");

        // 2. Recalculer les scores IA de priorité pour tout le monde
        waitingTickets.forEach(priorityService::calculateAndSetScore);

        // 3. --- L'ASTUCE --- On groupe les patients par ID de médecin assigné
        // Si aucun médecin n'est assigné, on les met ensemble dans le groupe -1L (triage général)
        java.util.Map<Long, List<Ticket>> ticketsByDoctor = waitingTickets.stream()
                .collect(Collectors.groupingBy(t -> t.getDoctor() != null ? t.getDoctor().getId() : -1L));

        // 4. On calcule l'ordre et le temps d'attente individuellement pour CHAQUE file de médecin
        ticketsByDoctor.forEach((doctorId, doctorTickets) -> {
            // On trie la file spécifique de CE médecin par score décroissant
            List<Ticket> sortedDoctorTickets = doctorTickets.stream()
                    .sorted(Comparator.comparingInt(Ticket::getPriorityScore).reversed()
                            .thenComparing(Ticket::getCreatedAt))
                    .collect(Collectors.toList());

            // On applique les positions et temps d'attente au sein de CETTE file uniquement
            for (int i = 0; i < sortedDoctorTickets.size(); i++) {
                Ticket t = sortedDoctorTickets.get(i);
                t.setPositionNumber(i + 1); // Position 1, 2, 3... pour CE médecin

                // Récupération du temps moyen du service du médecin
                int avgTime = 10;
                if (t.getDoctor() != null && t.getDoctor().getService() != null && t.getDoctor().getService().getAverageConsultationTime() != null) {
                    avgTime = t.getDoctor().getService().getAverageConsultationTime();
                } else if (t.getService() != null && t.getService().getAverageConsultationTime() != null) {
                    avgTime = t.getService().getAverageConsultationTime();
                }

                // Calcul du temps d'attente cloisonné
                t.setEstimatedWaitingTime(i * avgTime);
                ticketRepository.save(t);
            }
        });

        // 5. On retourne la liste complète triée globalement par score pour l'IHM du réceptionniste
        return waitingTickets.stream()
                .sorted(Comparator.comparingInt(Ticket::getPriorityScore).reversed()
                        .thenComparing(Ticket::getCreatedAt))
                .collect(Collectors.toList());
    }

    public Ticket createTicket(Ticket ticket) {
        // 1. Configuration initiale de base
        ticket.setStatus("WAITING");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setTicketNumber("PENDING"); // Libellé temporaire avant génération de l'ID
        priorityService.calculateAndSetScore(ticket);

        // 2. Première sauvegarde pour forcer MySQL à générer l'ID unique auto-incrémenté
        Ticket savedTicket = ticketRepository.save(ticket);

        // 3. Génération du numéro d'usage court et professionnel (Ex: ID 5 devient TCK-005)
        savedTicket.setTicketNumber(String.format("TCK-%03d", savedTicket.getId()));

        // Sauvegarde finale du numéro formaté
        ticketRepository.save(savedTicket);

        // 4. Recalcul complet de la file pour mettre à jour les positions et temps d'attente
        this.getPriorityQueue();

        // 5. Rechargement complet du ticket depuis la base de données
        Ticket updatedTicket = ticketRepository.findById(savedTicket.getId()).orElse(savedTicket);

        // 6. Envoi de l'email de confirmation au patient avec le numéro propre
        emailService.sendInitialEmail(updatedTicket);

        return updatedTicket;
    }

    @Scheduled(fixedRate = 30000) // S'exécute toutes les 30 secondes (
    public void checkAndSendReminders() {
        // 1. On récupère tous ceux qui attendent
        List<Ticket> waitingTickets = ticketRepository.findByStatus("WAITING");

        for (Ticket t : waitingTickets) {
            // 2. Si l'attente est <= 5 min ET qu'on n'a pas encore envoyé le mail de rappel
            if (t.getEstimatedWaitingTime() <= 5 && !Boolean.TRUE.equals(t.getReminderEmailSent())) {

                // 3. On envoie l'email via notre service
                emailService.sendReminderEmail(t);

                // 4. On marque le rappel comme "envoyé" pour ne pas harceler le patient
                t.setReminderEmailSent(true);
                ticketRepository.save(t);

                System.out.println("Robot : Rappel envoyé à " + t.getPatient().getFullName());
            }
        }
    }

    public void updateTicketStatus(Long id, String status) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        ticket.setStatus(status);

        // Gestion précise du cycle de vie du patient (Lifecycle)
        switch (status.toUpperCase()) {
            case "CALLED": ticket.setCalledAt(LocalDateTime.now()); break;
            case "IN_CONSULTATION": ticket.setConsultationStartedAt(LocalDateTime.now()); break;
            case "COMPLETED": ticket.setCompletedAt(LocalDateTime.now()); break;
        }
        ticketRepository.save(ticket);
    }

    public void markAsAbsent(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket introuvable"));

        ticket.setStatus("ABSENT"); // Change le statut selon l'ERD
        ticket.setCompletedAt(LocalDateTime.now()); // On ferme le ticket
        ticketRepository.save(ticket);

        // Crucial : On recalcule la file pour que les suivants avancent d'une place
        this.getPriorityQueue();
    }

    public Ticket assignDoctor(Long ticketId, Long doctorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        com.mediflow.entity.Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
                
        ticket.setDoctor(doctor);
        // Receptionist just assigns it, it stays WAITING until doctor starts it
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getDoctorQueue(Long doctorId) {
        return ticketRepository.findByDoctorIdAndStatusInOrderByPriorityScoreDesc(doctorId, List.of("WAITING", "IN_CONSULTATION"));
    }

    public Ticket startConsultation(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket introuvable"));
        ticket.setStatus("IN_CONSULTATION");
        ticket.setConsultationStartedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

}