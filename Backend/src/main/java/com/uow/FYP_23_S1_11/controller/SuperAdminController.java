package com.uow.FYP_23_S1_11.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uow.FYP_23_S1_11.service.SystemAdminService;

@RestController
@RequestMapping(value = "/api/sysAdmin", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
public class SuperAdminController {
    @Autowired
    private SystemAdminService sysAdminService;

    @GetMapping("/getAllClinics")
    public ResponseEntity<List<?>> getAllClinics() {
        return ResponseEntity.ok(sysAdminService.getAllClinics());
    }

}
