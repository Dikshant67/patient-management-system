package com.pm.analyticsservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient", groupId = "analytics-group")
    public void consume(byte[] eventBytes) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(eventBytes);
            // Perform business logic for analytics service.
            log.info("Received Kafka Patient Event - ID: {}, Name: {}, Email: {}, EventType: {}",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail(),
                    patientEvent.getEventType());
        } catch (Exception e) {
            log.error("Error parsing/processing Patient Event from Kafka: {}", e.getMessage(), e);
        }
    }
}
