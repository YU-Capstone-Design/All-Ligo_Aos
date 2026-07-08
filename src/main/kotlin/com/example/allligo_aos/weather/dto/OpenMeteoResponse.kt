package com.example.allligo_aos.weather.dto

import com.fasterxml.jackson.annotation.JsonProperty

// Open meteo의 응답 JSON 구조가 다음과 같이 생겼기에 이렇게 매핑함
// 카멜케이스와 스네이크케이스의 불일치로 매핑을 명시적으로 알려줘야함
data class OpenMeteoResponse(val current: Current) {


    // @param을 먼저 나오게
    data class Current(
        @param:JsonProperty("temperature_2m") val temperature2m: Double?,
        @param:JsonProperty("relative_humidity_2m") val relativeHumidity2m: Int?,
        @param:JsonProperty("apparent_temperature") val apparentTemperature: Double?,
        @param:JsonProperty("is_day") val isDay: Int?,
        val precipitation: Double?,
        @param:JsonProperty("weather_code") val weatherCode: Int?,
        @param:JsonProperty("wind_speed_10m") val windSpeed10m: Double?
    )
}
