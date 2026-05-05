package com.mediflow.service;

import com.mediflow.entity.Ticket;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envoie l'email de confirmation initial (SRS 17.1)
     */
    public void sendInitialEmail(Ticket ticket) {
        if (ticket.getPatient() == null || ticket.getPatient().getEmail() == null) return;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // Le "true" ici permet d'indiquer qu'on veut un message multipart (si besoin d'HTML)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // --- C'EST ICI QU'ON CHANGE LE NOM ---
            helper.setFrom("sososadiqui2004@gmail.com", "MediFlow AI Support");
            helper.setTo(ticket.getPatient().getEmail());
            helper.setSubject("MediFlow AI - Votre ticket a été créé");

            String text = String.format(
                    "Bonjour %s,\n\n" +
                            "Votre ticket a été créé avec succès.\n" +
                            "Ticket: [%s]\n" +
                            "Position actuelle: [%d]\n" + // Utilisation de %d pour un entier
                            "Attente estimée: [%d min]\n\n" +
                            "Merci de votre confiance. — L'équipe MediFlow AI",
                    ticket.getPatient().getFullName(),
                    ticket.getTicketNumber(),
                    ticket.getPositionNumber(),
                    ticket.getEstimatedWaitingTime()
            );

            helper.setText(text);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Erreur d'envoi : " + e.getMessage());
        }
    }

    /**
     * Envoie l'email de rappel de 5 minutes (SRS 17.2)
     */
    public void sendReminderEmail(Ticket ticket) {
        if (ticket.getPatient() == null || ticket.getPatient().getEmail() == null) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(ticket.getPatient().getEmail());
        message.setSubject("MediFlow AI - Votre tour approche");

        String serviceName = (ticket.getService() != null) ? ticket.getService().getName() : "Non spécifié";

        String text = String.format(
                "Bonjour %s,\n\n" +
                        "Votre tour approche.\n" +
                        "Ticket: [%s] | Service: [%s]\n" +
                        "Attente estimée: [%d min] | Statut: ALMOST READY\n\n" +
                        "Veuillez vous rapprocher de la salle d'attente.\n" +
                        "Merci. — MediFlow AI",
                ticket.getPatient().getFullName(),
                ticket.getTicketNumber(),
                serviceName,
                ticket.getEstimatedWaitingTime()
        );

        message.setText(text);

        try {
            mailSender.send(message);
            System.out.println("Email de rappel envoyé à : " + ticket.getPatient().getEmail());
        } catch (Exception e) {
            System.err.println("Erreur d'envoi du rappel : " + e.getMessage());
        }
    }
}