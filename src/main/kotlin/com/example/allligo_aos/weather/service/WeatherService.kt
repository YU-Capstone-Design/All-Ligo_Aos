package com.example.allligo_aos.weather.service

import com.example.allligo_aos.weather.dto.OpenMeteoResponse
import com.example.allligo_aos.weather.dto.WeatherCategory
import com.example.allligo_aos.weather.dto.WeatherInfo
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody

/**
 * 날씨 정보와 관련된 비즈니스 로직을 처리하는 서비스 입니다.
 */
@Service
class WeatherService(restClientBuilder: RestClient.Builder) {

    // 요청을 보내주는 객체, 어디로 요청을 보낼지 초기화
    private val restClient: RestClient = restClientBuilder
        // 요청할 API 주소 : 오픈 메테오 날씨예보 요청
        .baseUrl("https://api.open-meteo.com/v1/forecast")
        .build()

    /**
     * 날씨 정보 Fetch 서비스
     *
     * 날씨 정보를 실제로 가져오기 위한 fetch 메소드 입니다.
     *
     * @param lat 위도
     * @param lon 경도
     * @return 현재 날씨 정보 객체
     */
    fun fetchWeather(lat: Double, lon: Double): WeatherInfo {
        val response = restClient.get() // GET 요청 준비 객체
            .uri { uriBuilder -> // 요청 보낼 최종 URI를 조립하는 부분
                uriBuilder
                    .queryParam("latitude", lat) // 위도 파라미터
                    .queryParam("longitude", lon) // 경도 파라미터
                    .queryParam(
                        "current", // 그리고 현재 기준의 온도, 상대습도, 체감온도, 낮밤여부, 강수량, 날씨코드, 풍속 가져오기
                        "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m"
                    )
                    .build()
            }
            .retrieve() // 전송하기
            .requiredBody<OpenMeteoResponse>() // 응답을 어떤 코틀린 객체로 변환할지 설정, 클래스 불러오는 방법의 차이

        // 응답에서 실제 데이터가 들어있는 current 부분만 꺼내서 저장
        val current = response.current
        // 받은 응답의 날씨 코드를 이용하여 날씨 카테고리 가져오기 (밑 메소드 참고 바람)
        val category = mapWeatherCode(current.weatherCode)

        // 날씨 정보 반환
        return WeatherInfo(
            category.desc, // 한글 날씨 정보
            current.temperature2m, // 실제 섭씨 기온
            category.visualCue, // 날씨를 통한 분위기
            current.weatherCode, // 날씨 원본 숫자 기상 코드
            current.precipitation, // 강수량
            current.relativeHumidity2m, // 상대습도
            current.windSpeed10m, // 풍속
            current.apparentTemperature, // 체감온도
            current.isDay // 낮밤여부 1, 0
        )
    }

    /**
     * 날씨 코드와 카테고리 매핑
     *
     * when을 통한 간략한 코드로 변경됨 확인
     *
     * @param weatherCode 날씨코드
     * @return 카테고리 객체 반환
     */
    private fun mapWeatherCode(weatherCode: Int?): WeatherCategory = when (weatherCode) {
        null -> WeatherCategory("맑음", "sunny, bright, clear sky, vibrant") // 기본값
        in 1..3 -> WeatherCategory("구름 조금/흐림", "cloudy, soft lighting, overcast")
        45, 48 -> WeatherCategory("안개", "foggy, mysterious, misty, muted colors")
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 ->
            WeatherCategory("비", "rainy, wet streets, puddles, cinematic moody lighting, water drops")
        71, 73, 75, 77, 85, 86 -> WeatherCategory("눈", "snowy, winter wonderland, falling snow, cold, cozy")
        95, 96, 99 -> WeatherCategory("뇌우/폭풍", "stormy, lightning, dark dramatic clouds, heavy rain")
        else -> WeatherCategory("맑음", "sunny, bright, clear sky, vibrant")
    }
}
