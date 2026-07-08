package com.example.allligo_aos.marketing.controller

import com.example.allligo_aos.marketing.service.MarketingTextService
import com.example.allligo_aos.vision.dto.VisionAnalysis
import com.example.allligo_aos.weather.dto.WeatherInfo
import com.example.allligo_aos.weather.service.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/marketing")
// 마찬가지로 클래스의 파라미터로 들어가는건 생성자의 파라미터들임. 기존에 멤버필드로 만들었던걸 여기에다가 넣음.
// Kotlin에서는 이 한줄에서 멤버필드를 선언하고 즉시 생성자로써 초기화까지 해주는 역할까지 수행함.
// 필드 선언 + 생성자 파라미터 + 초기화
class MarketingTextController(
    private val weatherService: WeatherService, // private final를 private val로 씀
    private val marketingTextService: MarketingTextService
) {

    /**
     * 게시글 블로그 홍보 텍스트 생성 API
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param topPerformersContext 우수 게시물 정보
     * @param moodTag 기분 태그
     * @param hashTag 해시 태그
     * @param userPrompt 소상공인 프롬프트
     * @param uploadDay 업로드 날짜
     * @param uploadTime 업로드 시간
     * @return 홍보 텍스트 응답
     */
    @GetMapping("/generate-text")
    fun generateText(
        @RequestParam latitude: Double, // Kotlin의 Double형
        @RequestParam longitude: Double,
        @RequestParam(required = false, defaultValue = "") topPerformersContext: String,
        @RequestParam moodTag: String,
        @RequestParam hashTag: String,
        @RequestParam userPrompt: String,
        @RequestParam uploadDay: String,
        @RequestParam uploadTime: String
    ): ResponseEntity<*> {
        // ↓↓↓ TEMP-TEST: open-meteo.com 접속 장애로 임시 하드코딩. 네트워크 복구되면 이 블록 지우고
        //     weatherService.fetchWeather(latitude, longitude) 호출로 되돌릴 것.
        val dummyWeather = WeatherInfo(
            "맑음",
            26.3,
            "sunny, bright, clear sky, vibrant",
            0,
            0.0,
            55,
            3.2,
            27.1,
            1
        )
        // ↑↑↑ TEMP-TEST 끝

        // TEMP-TEST: 더미 비전 분석 결과
        val dummyVision = VisionAnalysis(
            listOf("coffee", "croissant", "wooden table"), // Java List.of -> Kotlin listOf
            listOf("cozy", "warm", "inviting"),
            listOf("brown", "cream", "white")
        )

        return ResponseEntity.ok(
            marketingTextService.generatePostText(
                dummyWeather, // TEMP-TEST: weatherService.fetchWeather(latitude, longitude) 대신 임시 사용
                dummyVision,
                topPerformersContext,
                moodTag,
                hashTag,
                userPrompt,
                uploadDay,
                uploadTime
            )
        )
    }

    /**
     * 숏폼 홍보 텍스트 생성 API
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param topPerformersContext 우수 게시물 정보
     * @param moodTag 기분 태그
     * @param hashTag 해시 태그
     * @param userPrompt 소상공인 프롬프트
     * @param uploadDay 업로드 날짜
     * @param uploadTime 업로드 시간
     * @return 홍보 텍스트 응답
     */
    @GetMapping("/generate-video-text")
    fun generateVideoText(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(required = false, defaultValue = "") topPerformersContext: String,
        @RequestParam moodTag: String,
        @RequestParam hashTag: String,
        @RequestParam userPrompt: String,
        @RequestParam uploadDay: String,
        @RequestParam uploadTime: String
    ): ResponseEntity<*> {
        // TEMP-TEST: 기존 엔드포인트와 동일한 더미 날씨/비전 데이터 재사용
        val dummyWeather = WeatherInfo(
            "맑음", 26.3, "sunny, bright, clear sky, vibrant",
            0, 0.0, 55, 3.2, 27.1, 1
        )
        val dummyVision = VisionAnalysis(
            listOf("coffee", "croissant", "wooden table"),
            listOf("cozy", "warm", "inviting"),
            listOf("brown", "cream", "white")
        )

        return ResponseEntity.ok(
            marketingTextService.generateVideoContent(
                dummyWeather,
                dummyVision,
                topPerformersContext,
                moodTag,
                hashTag,
                userPrompt,
                uploadDay,
                uploadTime
            )
        )
    }
}
