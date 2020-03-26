package com.potato.controller;

import com.potato.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/health")
public class HealthCheckController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public ResponseEntity check() {
        log.info("health");
        this.permissionService.save();
        return new ResponseEntity(  HttpStatus.OK);
    }

}

