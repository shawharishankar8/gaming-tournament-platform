package com.tournament.controller;

import com.tournament.model.dto.request.RegisterPlayerRequest;
import com.tournament.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterPlayerRequest request) {
        registrationService.register(request.getTournamentId(), request.getPlayerId());
        return ResponseEntity.noContent().build();
    }
}


