package com.mediflow.repository;

import com.mediflow.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Récupérer uniquement les patients qui attendent encore
    List<Ticket> findByStatusOrderByCreatedAtAsc(String status);
}