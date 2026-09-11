package org.pm.patientservice.service;

import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.exception.EmailAlreadyExistsException;
import org.pm.patientservice.exception.PatientNotFoundException;
import org.pm.patientservice.grpc.BillingServiceGrpcClient;
import org.pm.patientservice.kafka.KafkaProducer;
import org.pm.patientservice.mapper.PatientMapper;
import org.pm.patientservice.model.Patient;
import org.pm.patientservice.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final KafkaProducer kafkaProducer;
    private PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        log.info("Fetching all patients from database");
        List<Patient> patients = patientRepository.findAll();
        log.info("Successfully fetched {} patients from database", patients.size());
        return patients.stream().map(PatientMapper::toPatientResponseDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        log.info("Creating patient request received for email {}", patientRequestDTO.getEmail());
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            log.warn("Patient creation failed - email already exists: {}", patientRequestDTO.getEmail());
            throw new EmailAlreadyExistsException("A patient with this email already exists " + patientRequestDTO.getEmail());
        }
        Patient patient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));
        try {
            log.info("Calling billing service via gRPC for patient id {}", patient.getId());
            billingServiceGrpcClient.createBillingAccount(patient.getId().toString(), patient.getName(), patient.getEmail());
            kafkaProducer.sendEvent(patient);
        } catch (RuntimeException ex) {
            log.error("Billing account creation failed for patient id {}", patient.getId(), ex);
            throw ex;
        }
        log.info("Created patient with id {}", patient.getId().toString());
        return PatientMapper.toPatientResponseDTO(patient);
    }

    public PatientResponseDTO updatePatient(UUID patientId, PatientRequestDTO patientRequestDTO) {
        log.info("Updating patient details for ID: {}", patientId);
        Patient patient = patientRepository.findById(patientId).orElseThrow(
                ()-> {
                    log.warn("Update failed - Patient not found with ID: {}", patientId);
                    return new PatientNotFoundException("Patient not found with ID : "+ patientId);
                }
        );
        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),patientId)) {
            log.warn("Update failed - Email {} already exists for another patient", patientRequestDTO.getEmail());
            throw new EmailAlreadyExistsException("A patient with this email already exists " + patientRequestDTO.getEmail());
        }
        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient with ID: {} updated successfully", patientId);
        return PatientMapper.toPatientResponseDTO(updatedPatient);
    }
    public void deletePatient(UUID patientId) {
        log.info("Deleting patient with ID: {}", patientId);
        if (patientRepository.existsById(patientId)) {
            patientRepository.deleteById(patientId);
            log.info("Patient with ID: {} deleted successfully", patientId);
        } else {
            log.warn("Delete failed - Patient not found with ID: {}", patientId);
            throw new PatientNotFoundException("Patient not found with ID : "+ patientId);
        }
    }
}
