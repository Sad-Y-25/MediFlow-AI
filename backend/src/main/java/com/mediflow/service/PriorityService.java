package com.mediflow.service;

import com.mediflow.entity.Ticket;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PriorityService {

    public void calculateAndSetScore(Ticket ticket) {
        int score = 0;

        // 1. Facteur d'urgence (LOW=+10, MEDIUM=+30, HIGH=+60)
        if (ticket.getUrgencyLevel() != null) {
            switch (ticket.getUrgencyLevel().toUpperCase()) {
                case "HIGH": score += 60; break;
                case "MEDIUM": score += 30; break;
                case "LOW": score += 10; break;
            }
        }

        // 2. Facteur Rendez-vous (+15)
        if (Boolean.TRUE.equals(ticket.getHasAppointment())) {
            score += 15;
        }

        // 3. Facteur Âge (+10 si > 60 ans)
        if (ticket.getPatient() != null && ticket.getPatient().getAge() != null && ticket.getPatient().getAge() > 60) {
            score += 10;
        }

        // 4. Facteur Temps d'attente (+1 point par 5 minutes d'attente)
        if (ticket.getCreatedAt() != null) {
            long minutesWaited = Duration.between(ticket.getCreatedAt(), LocalDateTime.now()).toMinutes();
            score += (int) (minutesWaited / 5);
        }

        ticket.setPriorityScore(score);
    }
}