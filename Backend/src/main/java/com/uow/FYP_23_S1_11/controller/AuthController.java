package com.uow.FYP_23_S1_11.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.uow.FYP_23_S1_11.domain.request.RegisterClinicRequest;
import com.uow.FYP_23_S1_11.domain.request.LoginRequest;
import com.uow.FYP_23_S1_11.domain.request.RegisterPatientRequest;
import com.uow.FYP_23_S1_11.service.UserAccountService;

import jakarta.mail.Multipart;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/auth", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
public class AuthController {
    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/refresh")
    public void refresh(HttpServletRequest request,
            HttpServletResponse response, @CookieValue(value = "refreshToken", defaultValue = "") String token)
            throws StreamWriteException, DatabindException, IOException {
        userAccountService.refresh(request, response, token);
    }

    @PostMapping("/login")
    public void authenticate(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response, @CookieValue(value = "refreshToken", defaultValue = "") String token)
            throws StreamWriteException, DatabindException, IOException {
        userAccountService.authenticate(loginRequest, request, response, token);
    }

    @PostMapping(value = "/registerClinic", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Boolean> registerClinic(@Valid @ModelAttribute RegisterClinicRequest clinicReq) {
        Boolean result = userAccountService.registerClinicAccount(clinicReq);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/registerPatient")
    public ResponseEntity<Boolean> registerPatient(@Valid @RequestBody RegisterPatientRequest patientReq,
            HttpServletRequest request) {
        Boolean result = userAccountService.registerPatientAccount(patientReq, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request,
            HttpServletResponse response, @CookieValue(value = "refreshToken", defaultValue = "") String token) {
        userAccountService.logout(request, response, token);
    }
}
