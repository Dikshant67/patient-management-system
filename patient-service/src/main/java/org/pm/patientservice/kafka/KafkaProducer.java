package org.pm.patientservice.kafka;

import org.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String , byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient){
        PatientEvent event= PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();
        try {
            log.info("Sending PATIENT_CREATED event to topic 'patient' for patient ID: {}", patient.getId());
            kafkaTemplate.send("patient", event.toByteArray());
            log.info("Successfully dispatched PATIENT_CREATED event for patient ID: {}", patient.getId());
        } catch (Exception e) {
            log.error("Error sending Patient Created Event for patient ID {}: {}", patient.getId(), e.getMessage(), e);
        }
    }
}
