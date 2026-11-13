package com.pmo.nid.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pmo.nid.dto.BiometricAuthRequestDto;
import com.pmo.nid.service.NidService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/nid")
@RequiredArgsConstructor
public class NidController {
    private final NidService nidService;

    @PostMapping("/verify-nin")
    public ResponseEntity<?> verifyNin(@RequestBody Map<String, String> request) {
        var nin = request.get("nin");
        var isNinValid = nidService.verifyNin(nin);
        return ResponseEntity.ok(Map.of("valid", isNinValid));
    }

    @PostMapping("/citizen-data")
    public ResponseEntity<?> getCitizenData(@RequestBody Map<String, String> request) {
        var nin = request.get("nin");
        var data = nidService.getCitizenData(nin);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/biometric-auth")
    public ResponseEntity<?> biometricAuth(@RequestBody BiometricAuthRequestDto dto) {
        var data = nidService.authenticateWithFingerprint(dto);
        return ResponseEntity.ok(data.getBiometricAuthResult().value());
    }
}
