package com.uow.FYP_23_S1_11.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.management.Query;

import org.hibernate.boot.jaxb.mapping.NamedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uow.FYP_23_S1_11.Constants;
import com.uow.FYP_23_S1_11.domain.Clinic;
import com.uow.FYP_23_S1_11.domain.Doctor;
import com.uow.FYP_23_S1_11.domain.DoctorSchedule;
import com.uow.FYP_23_S1_11.domain.FrontDesk;
import com.uow.FYP_23_S1_11.domain.Nurse;
import com.uow.FYP_23_S1_11.domain.PatientFeedbackClinic;
import com.uow.FYP_23_S1_11.domain.Specialty;
import com.uow.FYP_23_S1_11.domain.UserAccount;
import com.uow.FYP_23_S1_11.domain.request.DoctorScheduleRequest;
import com.uow.FYP_23_S1_11.domain.request.RegisterDoctorRequest;
import com.uow.FYP_23_S1_11.domain.request.RegisterFrontDeskRequest;
import com.uow.FYP_23_S1_11.domain.request.RegisterNurseRequest;
import com.uow.FYP_23_S1_11.domain.response.StaffAccountDetails;
import com.uow.FYP_23_S1_11.enums.ERole;
import com.uow.FYP_23_S1_11.repository.DoctorRepository;
import com.uow.FYP_23_S1_11.repository.DoctorScheduleRepository;
import com.uow.FYP_23_S1_11.repository.FrontDeskRepository;
import com.uow.FYP_23_S1_11.repository.NurseRepository;
import com.uow.FYP_23_S1_11.repository.PatientFeedbackClinicRepository;
import com.uow.FYP_23_S1_11.repository.SpecialtyRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ClinicOwnerServiceImpl implements ClinicOwnerService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private DoctorScheduleRepository doctorScheduleRepo;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private NurseRepository nurseRepo;
    @Autowired
    private FrontDeskRepository frontDeskRepo;
    @Autowired
    private SpecialtyRepository specialtyRepo;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private PatientFeedbackClinicRepository patientFeedbackClinicRepo;

    @Override
    public List<?> getAllStaffs() {
        UserAccount account = Constants.getAuthenticatedUser();

        TypedQuery<StaffAccountDetails> query = entityManager.createQuery(
                "SELECT new com.uow.FYP_23_S1_11.domain.response.StaffAccountDetails(ua.accountId, d.name, ua.role, d.email) FROM UserAccount ua "
                        + "JOIN ua.doctor d WHERE d.doctorClinic = :clinic UNION "
                        + "SELECT new com.uow.FYP_23_S1_11.domain.response.StaffAccountDetails(ua.accountId,n.name, ua.role, n.email) FROM UserAccount ua "
                        + "JOIN ua.nurse n WHERE n.nurseClinic = :clinic UNION "
                        + "SELECT new com.uow.FYP_23_S1_11.domain.response.StaffAccountDetails(ua.accountId, fd.name, ua.role, fd.email) FROM UserAccount ua "
                        + "JOIN ua.frontDesk fd WHERE fd.frontDeskClinic = :clinic",
                StaffAccountDetails.class);
        query.setParameter("clinic", account.getClinic());
        return query.getResultList();
    }

    @Override
    public Boolean registerDoctor(
            List<RegisterDoctorRequest> registerDoctorRequest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            // get owner's clinic object...
            UserAccount userAccount = Constants.getAuthenticatedUser();
            Clinic clinic = userAccount.getClinic();

            List<Doctor> newDoctorList = new ArrayList<Doctor>();
            for (RegisterDoctorRequest object : registerDoctorRequest) {
                UserAccount newAccount = (UserAccount) mapper.convertValue(object, UserAccount.class);
                UserAccount registeredAccount = userAccountService.registerAccount(newAccount, object.getEmail(),
                        ERole.DOCTOR);
                List<Specialty> specialty = specialtyRepo.findByTypeIn(object.getSpecialty());

                Doctor newDoctor = (Doctor) mapper.convertValue(object, Doctor.class);

                newDoctor.setDoctorAccount(registeredAccount);
                newDoctor.setDoctorClinic(clinic);
                newDoctor.setDoctorSpecialty(specialty);

                newDoctorList.add(doctorRepository.save(newDoctor));

                List<DoctorScheduleRequest> schedule = object.getSchedule();
                if (schedule != null && schedule.size() > 0) {
                    List<DoctorSchedule> _schedule = schedule.stream()
                            .distinct()
                            .map(obj -> {
                                if (LocalTime.parse(obj.getStartTime()).isBefore(clinic.getOpeningHrs())) {
                                    throw new IllegalArgumentException(
                                            "Doctor's work start time should not be before the opening hours of the clinic...");
                                } else if (LocalTime.parse(obj.getEndTime()).isAfter(clinic.getClosingHrs())) {
                                    throw new IllegalArgumentException(
                                            "Doctor's end start time should not be after the closing hours of the clinic...");
                                }

                                DoctorSchedule newSchedule = (DoctorSchedule) mapper.convertValue(obj,
                                        DoctorSchedule.class);
                                newSchedule.setDoctor(newDoctor);
                                return newSchedule;
                            })
                            .collect(Collectors.toList());

                    doctorScheduleRepo.saveAll(_schedule);
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public Boolean registerNurse(List<RegisterNurseRequest> registerNurseReq) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            // get owner's clinic object...
            UserAccount userAccount = Constants.getAuthenticatedUser();
            Clinic clinic = userAccount.getClinic();

            List<Nurse> nurses = registerNurseReq.stream().map(obj -> {
                UserAccount newAccount = (UserAccount) mapper.convertValue(obj, UserAccount.class);
                UserAccount registeredAccount = userAccountService.registerAccount(newAccount, obj.getEmail(),
                        ERole.NURSE);

                Nurse nurse = (Nurse) mapper.convertValue(obj, Nurse.class);
                nurse.setNurseAccount(registeredAccount);
                nurse.setNurseClinic(clinic);

                return nurse;
            }).collect(Collectors.toList());

            nurseRepo.saveAll(nurses);
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public Boolean registerFrontDesk(List<RegisterFrontDeskRequest> registerFrontDeskReq) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            // get owner's clinic object...
            UserAccount userAccount = Constants.getAuthenticatedUser();
            Clinic clinic = userAccount.getClinic();

            List<FrontDesk> clerks = registerFrontDeskReq.stream().map(obj -> {
                UserAccount newAccount = (UserAccount) mapper.convertValue(obj, UserAccount.class);
                UserAccount registeredAccount = userAccountService.registerAccount(newAccount, obj.getEmail(),
                        ERole.FRONT_DESK);

                FrontDesk frontDesk = (FrontDesk) mapper.convertValue(obj, FrontDesk.class);
                frontDesk.setFrontDeskAccount(registeredAccount);
                frontDesk.setFrontDeskClinic(clinic);

                return frontDesk;
            }).collect(Collectors.toList());

            frontDeskRepo.saveAll(clerks);
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public Boolean insertDoctorSchedule(DoctorScheduleRequest doctorScheduleReq) {
        // TODO: implement checker for whether doctor is the same clinic as owner
        // TODO: implement to check whether schedule conflicts with original schedule
        // and if it is before opening hours or after closing hours
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        try {
            Optional<Doctor> doctor = doctorRepository.findById(doctorScheduleReq.getDoctorId());
            if (doctor.isEmpty()) {
                throw new IllegalArgumentException("Doctor not found!!!");
            }
            DoctorSchedule newSchedule = (DoctorSchedule) mapper.convertValue(doctorScheduleReq, DoctorSchedule.class);
            newSchedule.setDoctor(doctor.get());
            doctorScheduleRepo.save(newSchedule);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e);
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public List<PatientFeedbackClinic> getByClinicFeedbackId(Integer clinicFeedbackId) {
        List<PatientFeedbackClinic> patientFeedbackClinic = patientFeedbackClinicRepo
                .findByClinicFeedbackId(clinicFeedbackId);
        if (patientFeedbackClinic.isEmpty() == false) {
            return patientFeedbackClinic;
        } else {
            throw new IllegalArgumentException("Feedback not found...");
        }
    }

}
