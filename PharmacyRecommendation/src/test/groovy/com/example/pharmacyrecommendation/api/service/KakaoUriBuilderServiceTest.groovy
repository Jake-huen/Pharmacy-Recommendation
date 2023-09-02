package com.example.pharmacyrecommendation.api.service

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class KakaoUriBuilderServiceTest extends Specification {
    private KakaoUriBuilderService kakaoUriBuilderService

    def setup(){
        // 모든 feature method 전에 실행됨
        kakaoUriBuilderService = new KakaoUriBuilderService()
    }

    def "buildUriByAddressSearch - 한글 파라미터인 경우 정상적으로 인코딩"() {
        given:
        String address = "광나루로458"
        def charset = StandardCharsets.UTF_8

        when:
        def uri = kakaoUriBuilderService.buildUriByAddressSearch(address) // 타입 동적으로 선언함
        // 디코딩 시켜서 예상했던 결과값과 일치하는지 판단
        def decodeResult = URLDecoder.decode(uri.toString(), charset)

        then:
        // println "uri = $uri"
        decodeResult == "https://dapi.kakao.com/v2/local/search/address.json?query=광나루로458"
    }
}
