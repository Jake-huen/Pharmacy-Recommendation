package com.example.pharmacyrecommendation.api.service;

import com.example.pharmacyrecommendation.api.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoAddressSearchService {

    private final RestTemplate restTemplate;
    private final KakaoUriBuilderService kakaoUriBuilderService;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    @Retryable(
            value = {RuntimeException.class}, // 더 구체적으로 Exception을 정해줄 수 있다
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000) // 얼마만큼 delay를 줄것인지
    )
    public KakaoApiResponseDto requestAddressSearch(String address) {

        if (ObjectUtils.isEmpty(address)) return null;
        URI uri = kakaoUriBuilderService.buildUriByAddressSearch(address);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        HttpEntity httpEntity = new HttpEntity<>(headers);

        // kakao api 호출
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class).getBody();
    }

    @Recover // 원래 메소드의 리턴타입을 맞추어줘야 한다
    public KakaoApiResponseDto recover(RuntimeException e, String address) {
        log.error("All the retries failed: address: {}, error: {}", address, e.getMessage());
        return null;
    }
}
