package com.example.pharmacyrecommendation.pharmacy.service;

import com.example.pharmacyrecommendation.pharmacy.cache.PharmacyRedisTemplateService;
import com.example.pharmacyrecommendation.pharmacy.dto.PharmacyDto;
import com.example.pharmacyrecommendation.pharmacy.entity.Pharmacy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacySearchService {

    private final PharmacyRepositoryService pharmacyRepositoryService;
    private final PharmacyRedisTemplateService pharmacyRedisTemplateService;

    public List<PharmacyDto> searchPharmacyDtoList() {
        // redis
        List<PharmacyDto> pharmacyDtoList = pharmacyRedisTemplateService.findAll();
        if(!pharmacyDtoList.isEmpty()) {
            log.info("redis findAll success!");
            return pharmacyDtoList;
        }
        // redis에서 emptyList가 반환되었을때(redis에 문제가 있을때)는 데이터베이스에서 찾도록 한다.
        // db
        return pharmacyRepositoryService.findAll()
                .stream()
                .map(entity -> converToPharmacyDto(entity))
                .collect(Collectors.toList());
    }

    private PharmacyDto converToPharmacyDto(Pharmacy pharmacy) {
        return PharmacyDto.builder()
                .id(pharmacy.getId())
                .pharmacyName(pharmacy.getPharmacyName())
                .pharmacyAddress(pharmacy.getPharmacyAddress())
                .latitude(pharmacy.getLatitude())
                .longitude(pharmacy.getLongitude())
                .build();
    }
}
