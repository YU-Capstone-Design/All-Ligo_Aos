package com.example.allligo_aos.weather.record

data class WeatherInfo(
    val weatherDesc: String, // 날씨 상태를 한글로 표현(가공값)
    val temperature: Double?, // 실제 섭씨 기온
    val visualCue: String, // 이미지 분위기 힌트, weatherDesc에 대응되는 값이 존재함(하드코딩, 가공값)
    val weatherCode: Int?, // 날씨 원본 숫자 코드 (WMO 기상코드), 여러 상황에서 매핑 가능
    val precipitation: Double?, // 강수량
    val humidity: Int?, // 상대습도
    val windSpeed: Double?, // 풍속
    val apparentTemperature: Double?, // 체감온도
    val isDay: Int? // 낮밤 (1은 낮, 0은 밤)
)
