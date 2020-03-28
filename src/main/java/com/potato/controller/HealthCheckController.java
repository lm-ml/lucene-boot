package com.potato.controller;

import com.potato.dto.Airport;
import com.potato.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "/airports")
    public ResponseEntity getAirports(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "iATA", required = false) String iATA,
                                      @RequestParam(value = "latitude", required = false) Double latitude,
                                      @RequestParam(value = "longitude", required = false) Double longitude,
                                      @RequestParam(value = "city", required = false) String city,
                                      @RequestParam(value = "country", required = false) String country) {
        List<Airport> airportList = this.permissionService.getAirports(name, iATA, latitude, longitude, city, country);
        return new ResponseEntity(airportList, HttpStatus.OK);
    }

}

