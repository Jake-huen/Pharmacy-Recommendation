package com.example.pharmacyrecommendation.pharmacy.service;

import com.example.pharmacyrecommendation.api.dto.DocumentDto;
import com.example.pharmacyrecommendation.api.dto.KakaoApiResponseDto;
import com.example.pharmacyrecommendation.api.service.KakaoAddressSearchService;
import com.example.pharmacyrecommendation.direction.dto.directionDto;
import com.example.pharmacyrecommendation.direction.entity.Direction;
import com.example.pharmacyrecommendation.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    // 로드뷰 URL
    private static final String ROAD_VIEW_BASE_URL = "https://map.kakao.com/link/roadview/";
    // 길안내 URL
    private static final String DIRECTION_BASE_URL = "https://map.kakao.com/link/map/";

    public List<directionDto.OutputDto> recommendPharmacyList(String address) {
        // 문자열 기반 주소를 입력받고 문자열 기반 주소 정보를 위치 기반 데이터로 변경해줌.

        // 카카오 주소 검색 API를 통해서 위치 기반 데이터로 변경
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        // validation 체크
        if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address: {}", address);
            return Collections.emptyList();
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

        // 가까운 약국 리스트
        // 1. 공공기관 약국 데이터 및 거리계산 알고리즘 이용 -> 가까운 약국을 찾는 방법
        // List<Direction> directionList = directionService.buildDirectionList(documentDto);

        // 2. Kakao 카테고리를 이용한 장소 검색 api 이용
        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        return directionService.saveAll(directionList)
                .stream()
                .map(this::convertToOutputDto)
                .collect(Collectors.toList());
    }

    // Direction Entity를 결과 화면으로 뿌려주기 위해서 OutputDto로 만듬
    private directionDto.OutputDto convertToOutputDto(Direction direction) {

        String params = String.join(",", direction.getTargetPharmacyName(),
                String.valueOf(direction.getTargetLatitude()), String.valueOf(direction.getTargetLongitude()));

        String result = UriComponentsBuilder.fromHttpUrl(DIRECTION_BASE_URL + params)
                .toUriString();

        log.info("direction params: {}, url: {}", params, result);

        return directionDto.OutputDto.builder()
                .pharmacyAddress(direction.getTargetAddress())
                .pharmacyName(direction.getTargetPharmacyName())
                .directionUrl(result)
                .roadViewUrl(ROAD_VIEW_BASE_URL + direction.getTargetLatitude() + "," + direction.getTargetLongitude())
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }
}
