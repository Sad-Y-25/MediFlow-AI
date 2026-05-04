package com.mediflow.controller;

import com.mediflow.entity.Ticket;
import com.mediflow.service.TicketService;
import com.mediflow.repository.TicketRepository; // Import manquant
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin("*")
public class TicketController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository; // 1. Déclarer la variable

    // 2. Mettre à jour le constructeur pour injecter les deux
    public TicketController(TicketService ticketService, TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/queue")
    public List<Ticket> getQueue() {
        return ticketService.getPriorityQueue();
    }

    @PostMapping("/add")
    public Ticket addTicket(@RequestBody Ticket ticket) {
        return ticketService.createTicket(ticket);
    }

    // 3. Ta nouvelle méthode fonctionnera maintenant sans erreur
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeTicket(@PathVariable Long id) {
        ticketService.updateTicketStatus(id, "COMPLETED");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public List<Ticket> getHistory() {
        return ticketRepository.findByStatus("COMPLETED");
    }
}