package com.mediflow.controller;

import com.mediflow.entity.Ticket;
import com.mediflow.service.TicketService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin("*")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/queue")
    public List<Ticket> getQueue() {
        return ticketService.getWaitingQueue();
    }

    @PostMapping("/add")
    public Ticket addTicket(@RequestBody Ticket ticket) {
        return ticketService.createTicket(ticket);
    }
}