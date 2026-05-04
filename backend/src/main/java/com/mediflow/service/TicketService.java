package com.mediflow.service;

import com.mediflow.entity.Ticket;
import com.mediflow.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getWaitingQueue() {
        return ticketRepository.findAll(); // Récupère tout sans tri pour tester
    }

    public Ticket createTicket(Ticket ticket) {
        ticket.setStatus("WAITING");
        return ticketRepository.save(ticket);
    }
}