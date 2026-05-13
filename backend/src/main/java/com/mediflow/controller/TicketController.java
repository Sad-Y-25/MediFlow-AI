package com.mediflow.controller;

import com.mediflow.entity.Ticket;
import com.mediflow.service.TicketService;
import com.mediflow.repository.TicketRepository; // Import manquant
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        // On récupère les tickets terminés ou absents
        return ticketRepository.findAll().stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()) || "ABSENT".equals(t.getStatus()))
                .sorted(Comparator.comparing(Ticket::getCompletedAt).reversed()) // Les plus récents en haut
                .collect(Collectors.toList());
    }
    @PutMapping("/{id}/absent")
    public ResponseEntity<Void> markAbsent(@PathVariable Long id) {
        // Appelle la logique métier pour changer le statut en "ABSENT"
        ticketService.markAsAbsent(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/assign/{doctorId}")
    public ResponseEntity<Ticket> assignDoctor(@PathVariable Long id, @PathVariable Long doctorId) {
        return ResponseEntity.ok(ticketService.assignDoctor(id, doctorId));
    }

    @GetMapping("/doctor/{doctorId}/queue")
    public List<Ticket> getDoctorQueue(@PathVariable Long doctorId) {
        return ticketService.getDoctorQueue(doctorId);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Ticket> startConsultation(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.startConsultation(id));
    }
}