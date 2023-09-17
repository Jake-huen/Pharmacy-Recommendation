package com.example.pharmacyrecommendation.direction.dto;

import lombok.*;

@Data
public class directionDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OutputDto {
        private String pharmacyName;
        private String pharmacyAddress;
        private String directionUrl;
        private String roadViewUrl;
        private String distance;
    }
}
