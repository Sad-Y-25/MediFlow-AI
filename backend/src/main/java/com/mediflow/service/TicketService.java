package com.mediflow.service;

import com.mediflow.entity.Ticket;
import com.mediflow.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Récupère la file d'attente triée par l'algorithme de priorité MediFlow AI.
     */
    public List<Ticket> getPriorityQueue() {
        List<Ticket> waitingTickets = ticketRepository.findByStatus("WAITING");

        return waitingTickets.stream()
                .sorted(Comparator
                        .comparingInt(Ticket::getUrgencyLevel).reversed()
                        // Utilise nullsLast pour éviter que le programme ne plante si la date est nulle
                        .thenComparing(Ticket::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Enregistre un nouveau patient dans le système.
     */
    public Ticket createTicket(Ticket ticket) {
        // Initialisation par défaut si nécessaire
        if (ticket.getStatus() == null) {
            ticket.setStatus("WAITING");
        }
        return ticketRepository.save(ticket);
    }

    /**
     * Met à jour le statut d'un ticket (ex: passage en consultation ou terminé).
     */
    public Ticket updateTicketStatus(Long id, String newStatus) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setStatus(newStatus);
                    return ticketRepository.save(ticket);
                })
                .orElseThrow(() -> new RuntimeException("Ticket avec l'ID " + id + " non trouvé"));
    }

    /**
     * Supprime un ticket (annulation).
     */
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}