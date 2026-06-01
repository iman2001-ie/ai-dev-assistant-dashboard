package com.example.aidevdashboard.controller;

import com.example.aidevdashboard.dto.SeedDataClaimResponse;
import com.example.aidevdashboard.service.SeedDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
public class DevDataController {
    private final SeedDataService seedDataService;

    public DevDataController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping("/claim-seed-data")
    public SeedDataClaimResponse claimSeedData() {
        return seedDataService.claimUnownedSeedData();
    }
}
