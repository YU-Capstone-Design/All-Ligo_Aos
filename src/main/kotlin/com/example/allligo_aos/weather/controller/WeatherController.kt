package com.example.allligo_aos.weather.controller

import com.example.allligo_aos.weather.service.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
class WeatherController(private val weatherService: WeatherService) {

    /**
     * 날씨 정보 Fetch API
     *
     * @param latitude 위도
     * @param longitude 경도
     * @return 날씨정보
     */
    @GetMapping
    fun getWeatherInfo(@RequestParam latitude: Double, @RequestParam longitude: Double): ResponseEntity<*> {
        return ResponseEntity.ok(weatherService.fetchWeather(latitude, longitude))
    }
}
