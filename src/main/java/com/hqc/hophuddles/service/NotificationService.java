package com.hqc.hophuddles.service;

import com.hqc.hophuddles.entity.AIGenerationJob;
import com.hqc.hophuddles.entity.DeliverySchedule;
import com.hqc.hophuddles.entity.HuddleSequence;
import com.hqc.hophuddles.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Async
@Slf4j
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHuddleReleaseNotification(List<User> users, HuddleSequence sequence) {
        for (User user : users) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("New Training Available: " + sequence.getTitle());
                message.setText(String.format(
                        "Hi %s,\n\nA new training huddle is now available: %s\n\nDescription: %s\n\nPlease log in to complete your training.",
                        user.getName(), sequence.getTitle(), sequence.getDescription()
                ));
                mailSender.send(message);
                log.info("Sent release notification to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send notification to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    public void sendHuddleReminderNotification(List<User> users, HuddleSequence sequence) {
        // Similar implementation for reminders
    }

    public void sendGenerationCompleteNotification(AIGenerationJob job) {
        // Notify content creators when AI generation completes
    }

    public void sendGenerationFailureNotification(AIGenerationJob job) {
        // Notify on AI generation failures
    }

    public void sendScheduleFailureAlert(DeliverySchedule schedule, String errorMessage) {
        // Alert administrators about scheduling failures
    }
}