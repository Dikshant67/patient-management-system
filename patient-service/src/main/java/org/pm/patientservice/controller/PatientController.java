package org.pm.patientservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import org.pm.patientservice.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name="Patient",description = "API for Managing Patients")
public class PatientController {
    private static final Logger log = LoggerFactory.getLogger(PatientController.class);
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }
    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        log.info("REST request to get all patients");
        List<PatientResponseDTO> patientResponseDTOS = patientService.getPatients();
        log.info("Returning {} patients", patientResponseDTOS.size());
        return ResponseEntity.ok().body(patientResponseDTOS);
    }
    @PostMapping
    @Operation(summary = "Create a new Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        log.info("REST request to create patient with email: {}", patientRequestDTO.getEmail());
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);
        log.info("Patient created successfully with ID: {}", patientResponseDTO.getId());
        return ResponseEntity.ok().body(patientResponseDTO);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update a existing Patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        log.info("REST request to update patient with ID: {}", id);
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id,patientRequestDTO);
        log.info("Patient updated successfully with ID: {}", id);
        return ResponseEntity.ok().body(patientResponseDTO);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        log.info("REST request to delete patient with ID: {}", id);
        patientService.deletePatient(id);
        log.info("Patient deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}

