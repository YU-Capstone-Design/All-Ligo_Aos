package com.example.allligo_aos.weather.record

/**
 * 날씨 카테고리
 *
 * API로 가져온 날씨 정보를 이용하여 프롬프트 주입을 위한 파생 필드 생성 레코드 입니다.
 *
 * @property desc 날씨 정보 한글화
 * @property visualCue 날씨 분위기
 */
data class WeatherCategory(val desc: String, val visualCue: String)
