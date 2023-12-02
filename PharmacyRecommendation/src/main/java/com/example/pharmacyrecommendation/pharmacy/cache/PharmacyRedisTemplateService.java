package com.example.pharmacyrecommendation.pharmacy.cache;


import com.example.pharmacyrecommendation.pharmacy.dto.PharmacyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRedisTemplateService {

    private static final String CACHE_KEY = "PHARMACY";

    private final RedisTemplate<String, Object> redisTemplate;
    // ObjectMapper를 빈으로 등록해놓고, 싱글톤으로 사용할 예정
    // Serialize나 Deserialize를 ObjectMapper를 통해서 사용
    private final ObjectMapper objectMapper;

    // 캐시 키(약국 키 값), 서브 키(약국 데이터의 PK 값), value(약국 DTO를 String으로 변경)
    private HashOperations<String, String, String> hashOperations;

    // @PostConstruct -> 생성자 주입이 이뤄지고 난 이후에 REST Template에서 제공하는 해시 자료 구조를 이용할 것
    @PostConstruct
    public void init() {
        this.hashOperations = redisTemplate.opsForHash();
    }

    // Redis에 저장하는 부분
    public void save(PharmacyDto pharmacyDto) {
        if (Objects.isNull(pharmacyDto) || Objects.isNull(pharmacyDto.getId())) {
            log.error("Required Values mus not be null");
            return;
        }
        try {
            hashOperations.put(CACHE_KEY,
                    pharmacyDto.getId().toString(),
                    serializePharmacyDto(pharmacyDto));
            log.info("[PharmacyRedisTemplateService save succees] id: {}", pharmacyDto.getId());
        } catch (Exception e) {
            log.error("[PharmacyRedisTemplateService save error] {}", e.getMessage());
        }
    }

    // 전체를 조회하는 부분
    public List<PharmacyDto> findAll() {
        try {
            List<PharmacyDto> list = new ArrayList<>();
            for (String value : hashOperations.entries(CACHE_KEY).values()) {
                PharmacyDto pharmacyDto = deserializePharmacyDto(value);
                list.add(pharmacyDto);
            }
            return list;
        } catch (Exception e) {
            log.info("[PharmacyRedisTemplateService findAll error] {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void delete(Long id) {
        hashOperations.delete(CACHE_KEY, String.valueOf(id));
        log.info("[PharmacyRedisTemplateService delete] id: {}", id);
    }

    // Serialize
    private String serializePharmacyDto(PharmacyDto pharmacyDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(pharmacyDto);
    }

    // Deserialize
    private PharmacyDto deserializePharmacyDto(String value) throws JsonProcessingException {
        return objectMapper.readValue(value, PharmacyDto.class);
    }
}
