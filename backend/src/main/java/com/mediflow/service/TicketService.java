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
        List<Ticket> waitingTickets = ticketRepository.findByStatus("WAITING");

        // 1. Recalculer les scores IA pour tout le monde
        waitingTickets.forEach(priorityService::calculateAndSetScore);

        // 2. Trier : Score le plus élevé en premier, puis par date (FIFO) en cas d'égalité
        waitingTickets = waitingTickets.stream()
                .sorted(Comparator.comparingInt(Ticket::getPriorityScore).reversed()
                        .thenComparing(Ticket::getCreatedAt))
                .collect(Collectors.toList());

        // 3. Assigner les positions et le temps estimé
        for (int i = 0; i < waitingTickets.size(); i++) {
            Ticket t = waitingTickets.get(i);
            t.setPositionNumber(i + 1);

            // Sécurité : On récupère le temps moyen, sinon 10 min par défaut
            int avgTime = (t.getService() != null && t.getService().getAverageConsultationTime() != null)
                    ? t.getService().getAverageConsultationTime() : 10;

            t.setEstimatedWaitingTime(i * avgTime);
            ticketRepository.save(t);
        }

        return waitingTickets;
    }

    public Ticket createTicket(Ticket ticket) {
        // 1. Setup de base
        ticket.setStatus("WAITING");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setTicketNumber("TCK-" + System.currentTimeMillis());
        priorityService.calculateAndSetScore(ticket);

        // 2. Sauvegarde initiale
        ticketRepository.save(ticket);

        // 3. --- CRUCIAL --- On recalcule toute la file pour avoir les positions exactes
        // Cette méthode met à jour les positionNumber et estimatedWaitingTime de tout le monde
        this.getPriorityQueue();

        // 4. On recharge le ticket mis à jour depuis la DB pour avoir les vraies valeurs
        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElse(ticket);

        // 5. Envoi de l'email avec les données réelles (non nulles)
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