package com.example.pharmacyrecommendation.api.service

import com.example.pharmacyrecommendation.AbstractIntegrationContainerBaseTest
import com.example.pharmacyrecommendation.api.dto.DocumentDto
import com.example.pharmacyrecommendation.api.dto.KakaoApiResponseDto
import com.example.pharmacyrecommendation.api.dto.MetaDto
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

class KakaoAddressSearchServiceRetryTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private KakaoAddressSearchService kakaoAddressSearchService

    // @MockBean -> Spring 내에 있는 Bean을 Mocking 하겠다는 의미 / 이렇게 하는 이유는 실제 카카오 API 서비스를 호출하는게 아니라
    //              localhost에 있는 Mocking Server를 호출하는 것 -> Spring Container 내에 있는 서비스를 Mocking하는 것
    @SpringBean
    // Spock에서 사용하는 MockBean과 동일
    private KakaoUriBuilderService kakaoUriBuilderService = Mock()

    private MockWebServer mockWebServer

    private ObjectMapper mapper = new ObjectMapper()

    private String inputAddress = "서울 성북구 종암로 10길"

    def setup() {
        mockWebServer = new MockWebServer()
        mockWebServer.start()
        println mockWebServer.port
        println mockWebServer.url("/")
    }

    def cleanup() {
        mockWebServer.shutdown()
    }

    // Mock Web Server의 응답값을 조절해 줄 수 있다
    def "requestAddressSearch retry success"() {
        given:
        def metaDto = new MetaDto(1)
        def documentDto = DocumentDto.builder()
                .addressName(inputAddress)
                .build()
        def expectedResponse = new KakaoApiResponseDto(metaDto, Arrays.asList(documentDto))
        def uri = mockWebServer.url("/").uri()

        when:
        mockWebServer.enqueue(new MockResponse().setResponseCode(504)) // 첫번째 호출은 일부로 에러
        mockWebServer.enqueue(new MockResponse().setResponseCode(200) // 두번째에 정상값
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)))

        def kakaoApiResult = kakaoAddressSearchService.requestAddressSearch(inputAddress)

        then:
        2 * kakaoUriBuilderService.buildUriByAddressSearch(inputAddress) >> uri // MockUri를 리턴하도록 함
        // 실제 2번 호출된게 맞는지 확인
        kakaoApiResult.getDocumentList().size() == 1
        kakaoApiResult.getMetaDto().totalCount == 1
        kakaoApiResult.getDocumentList().get(0).getAddressName() == inputAddress
    }

    def "requestAddressSearch retry fail"() {
        given:
        def uri = mockWebServer.url("/").uri()

        when:
        mockWebServer.enqueue(new MockResponse().setResponseCode(504))
        mockWebServer.enqueue(new MockResponse().setResponseCode(504))

        def result = kakaoAddressSearchService.requestAddressSearch(inputAddress)

        then:
        2 * kakaoUriBuilderService.buildUriByAddressSearch(inputAddress) >> uri
        result == null // 2번다 실패하면 null 값을 반환하게 됨

    }
}
