package com.example.pharmacyrecommendation.direction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class directionDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InputDto {
        private String address;
    }

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
