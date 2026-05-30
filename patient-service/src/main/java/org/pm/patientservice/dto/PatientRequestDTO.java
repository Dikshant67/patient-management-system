package org.pm.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.pm.patientservice.dto.validators.CreatePatientValidationGroup;

public class PatientRequestDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100,message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is Required")
    @Email(message = "Email Should be valid")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Date of Birth is required")
    private String dateOfBirth;


    @NotBlank(groups = CreatePatientValidationGroup.class, message = "Registration date is required")
    private String registeredDate;
    public String getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }




}
