package com.uow.FYP_23_S1_11.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uow.FYP_23_S1_11.enums.EGender;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PATIENT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Patient implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer patientId;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;
    @Temporal(TemporalType.DATE)
    private Date dob;
    private String address;
    @Enumerated(EnumType.STRING)
    private EGender gender;
    private Integer contactNo;

    @OneToOne
    @JoinColumn(name = "patientAccount", referencedColumnName = "accountId")
    private UserAccount patientAccount;

    @OneToMany(mappedBy = "patient")
    private List<PatientFeedback> patientFeedback;

    @OneToMany(mappedBy = "apptPatient")
    @JsonIgnore
    private List<Appointment> patientAppt;

    @JsonIgnore
    @OneToOne(mappedBy = "patientmd", cascade = CascadeType.ALL)
    private PatientMedicalRecords patientMedicalRecords;

    @OneToMany(mappedBy = "patientClinicFeedback")
    @JsonIgnore
    private List<PatientFeedbackClinic> feedbackPatientClinic;

    @OneToMany(mappedBy = "patientDoctorFeedback")
    @JsonIgnore
    private List<PatientFeedbackDoctor> feedbackPatientDoctor;

}
