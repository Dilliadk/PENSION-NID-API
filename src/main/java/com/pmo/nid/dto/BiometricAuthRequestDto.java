package com.pmo.nid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BiometricAuthRequestDto {
    private String nin;
    private String fingerprintPosition;
    private byte[] imageData;
}
