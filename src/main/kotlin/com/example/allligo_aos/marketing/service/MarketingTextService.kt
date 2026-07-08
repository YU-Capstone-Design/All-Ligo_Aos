package com.example.allligo_aos.marketing.service

import com.example.allligo_aos.marketing.record.MarketingContent
import com.example.allligo_aos.vision.record.VisionAnalysis
import com.example.allligo_aos.weather.record.WeatherInfo
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
// 이때 파라미터 내부에 들어가는 chatClientBuilder는 필드가 아님. val같은것도 없으며 생성자 실행중에 잠깐 쓰이는 지역변수로 취급하기
class MarketingTextService(chatClientBuilder: ChatClient.Builder) {

    // 진짜로 그 정보를 저장해둘 필드는 여기임. 여기다가 아까 임시변수 가져와서 빌드 하고 저장
    private val chatClient: ChatClient = chatClientBuilder.build()

    /**
     * 날씨 정보 컨텍스트 빌드 메소드
     *
     * @param weather 날씨 정보
     * @return 날씨 관련하여 만들어진 프롬프트
     */
    private fun buildWeatherContext(weather: WeatherInfo): String {
        // 날씨 정보를 가져왔을때 1이면 낮, null이거나 0이면 밤
        // 삼항 연산자를 쓸 수 없는 대신에 if else를 한줄로 가능
        val isDayStr = if (weather.isDay == 1) "낮" else "밤"

        // 스트링 빌더 대신 buildString{} 으로 바로 가능
        // 또는 val sb = StringBuilder() 로도 가능
        return buildString {
            // 템플릿 엔진처럼 문자열 끼워넣기 가능
            append("현재 위치의 날씨는 '${weather.weatherDesc}'이며, 기온은 ${weather.temperature}도(체감 ${weather.apparentTemperature}도)입니다. ")
            append("현재 시간대는 ${isDayStr}이며, 습도는 ${weather.humidity}%, 풍속은 ${weather.windSpeed}m/s입니다. ")

            // 비가 올 경우
            // 빌드 스트링 도중에 조건문 사용 가능
            if (weather.precipitation != null && weather.precipitation > 0) {
                append("현재 강수량은 ${weather.precipitation} mm로 비나 눈이 내리고 있습니다. ")
            }

            append("이미지 프롬프트 작성 시 시각적 분위기 힌트(${weather.visualCue})와, 방금 말씀드린 날씨 정보를 적극 활용하여 현장감 있는 홍보물을 만드세요.")
        }
    }

    /**
     * 시각 정보 컨텍스트 문자열 생성
     */
    private fun buildVisionContext(vision: VisionAnalysis?): String {
        if (vision == null) {
            return ""
        }

        // 각각 리스트 자료형으로 받으므로 저 구분자를 통해 원소를 연속해서 받아 하나의 문자열로 합침
        return """
                - 주요 객체: ${vision.objects.joinToString(", ")}
                - 분위기: ${vision.mood.joinToString(", ")}
                - 주요 색상: ${vision.colors.joinToString(", ")}
                이 분석 결과를 바탕으로 새로운 마케팅 텍스트를 작성하세요.

                """.trimIndent() // 앞뒤 공백 제거 + 여러 줄 문자열의 공통으로 붙은 들여쓰기 제거(Java에서의 stripIndent())
    }

    /**
     * 이전 우수 게시물 탐색
     *
     * 이 로직은 RAG가 완성되기전까지 가볍게 작성되었습니다.
     *
     * @param topPerformersContext 우수 게시물 평문으로 가져오기
     * @return 우수 게시물 프롬프트 작성
     */
    private fun buildPerformersContext(topPerformersContext: String?): String {
        // null과 empty여부를 한번에 확인 가능. or연산
        if (topPerformersContext.isNullOrEmpty()) {
            return ""
        }

        // 변수 하나 단독으로 표현식이 들어가지 않을 경우 $만 붙임. 객체처럼 표현식으로 접근할때는 ${}
        return """
            $topPerformersContext

            아래의 [과거 우수 성과 게시물 레퍼런스]는 우리 매장에서 반응이 가장 좋았던 홍보물들입니다.
            이 텍스트들의 문체, 감성, 길이를 분석하고 모방하여 이번 타겟 시간대와 날씨에 맞는 새로운 홍보 텍스트를 작성해 주세요.

            """.trimIndent()
    }

    /**
     * 블로그용 글 생성
     *
     * @param weather 날씨정보
     * @param vision 이미지 분석 정보
     * @param topPerformersContext 우수 게시물 정보
     * @param moodTag 기분태그
     * @param hashTag 해시태그
     * @param userPrompt 소상공인 프롬프트
     * @param uploadDay 업로드 날짜
     * @param uploadTime 업로드 시간
     * @return 생성된 블로그 글
     */
    fun generatePostText(
        weather: WeatherInfo,
        vision: VisionAnalysis?,
        topPerformersContext: String?,
        moodTag: String,
        hashTag: String,
        userPrompt: String,
        uploadDay: String,
        uploadTime: String
    ): String? {
        // 날씨 컨텍스트 불러오기
        val weatherContext = buildWeatherContext(weather)
        // 비전 분석 결과 불러오기
        val visionContext = buildVisionContext(vision)
        // 우수 게시물 불러오기
        val performersContext = buildPerformersContext(topPerformersContext)

        val template = """
            당신은 소상공인을 돕는 전문 마케터입니다. 아래 정보를 바탕으로 매력적인 홍보 텍스트를 작성하세요.
            이미지 생성은 하지 않으므로 [IMAGE_PROMPT]는 절대 작성하지 마세요.

            [실시간 날씨 컨텍스트]
            {weatherContext}

            [마케팅 정보]
            - 분위기 태그: {moodTag}
            - 해시태그: {hashTag}
            - 사용자 추가 요청: {userPrompt}
            - 업로드 예정 요일: {uploadDay}
            - 업로드 예정 시간: {uploadTime}
            - 소상공인이 홍보를 희망하는 대상에 대한 이미지 분석 결과: {visionContext}
            - 과거 우수 성과물 레퍼런스: {performersContext}

            중요 지시사항:
            1. 블로그나 인스타그램 포스트용이므로, 이모지를 포함하여 3문단 이상의 충분한 길이로 상세한 홍보 글을 작성하세요.
            2. "내용 :", "마케팅 문구 :" 등 어떠한 메타 텍스트나 접두사도 절대 포함하지 마세요. 오직 실제 사용될 텍스트만 작성하세요.
            3. 홍보 텍스트는 [실시간 날씨 컨텍스트]의 기상 상황을 자연스럽게 반영하여 작성하세요.
            4. 업로드 예정 시간 한국시간 기준으로 ({uploadDay} {uploadTime})에 맞는 타겟 독자 상황을 고려하세요.
            5. 해시태그({hashTag})의 의미만 홍보 텍스트 내용에 자연스럽게 반영하되, 텍스트 내에 '#' 기호나 해시태그 단어 자체는 절대 포함하지 마세요.

            출력 형식:
            (여기에 순수 홍보 텍스트만 작성)
            """.trimIndent()

        // 이건 Java에서 stream형으로 쓴거랑 완벽히 동일함
        return chatClient.prompt()
            // 위에 쓴 템플릿에 변수명이 동일한 파라미터들 전달하여 삽입
            .user { u ->
                u.text(template)
                    .param("weatherContext", weatherContext)
                    .param("visionContext", visionContext)
                    .param("performersContext", performersContext)
                    .param("moodTag", moodTag)
                    .param("hashTag", hashTag)
                    .param("userPrompt", userPrompt)
                    .param("uploadDay", uploadDay)
                    .param("uploadTime", uploadTime)
            }.call()
            .content()
    }

    /**
     * 숏폼 캡션용 텍스트 생성
     *
     * @param weather 날씨정보
     * @param vision 이미지 분석 정보
     * @param topPerformersContext 우수 게시물 정보
     * @param moodTag 기분태그
     * @param hashTag 해시태그
     * @param userPrompt 소상공인 프롬프트
     * @param uploadDay 업로드 날짜
     * @param uploadTime 업로드 시간
     * @return 생성된 숏폼 캡션
     */
    fun generateVideoContent(
        weather: WeatherInfo,
        vision: VisionAnalysis?,
        topPerformersContext: String?,
        moodTag: String,
        hashTag: String,
        userPrompt: String,
        uploadDay: String,
        uploadTime: String
    ): MarketingContent? {
        // 날씨 정보 가져오기
        val weatherContext = buildWeatherContext(weather)
        // 비전 분석 결과 불러오기
        val visionContext = buildVisionContext(vision)
        // 우수 게시물 불러오기
        val performersContext = buildPerformersContext(topPerformersContext)

        val template = """
            당신은 소상공인을 돕는 전문 마케터입니다. 아래 정보를 바탕으로 짧고 강렬한 숏폼 영상 자막용 홍보 텍스트를 작성하고,
            포스터 이미지를 만들기 위한 영어 이미지 프롬프트 3개도 함께 작성하세요.
            날씨에 어울리는 시각적 분위기와 분위기 태그의 감성을 반영한, 서로 다른 고품질 이미지 프롬프트를 영어로 작성하세요.

            [실시간 날씨 컨텍스트]
            {weatherContext}

            [마케팅 정보]
            - 분위기 태그: {moodTag}
            - 해시태그: {hashTag}
            - 사용자 추가 요청: {userPrompt}
            - 업로드 예정 요일: {uploadDay}
            - 업로드 예정 시간: {uploadTime}
            - 소상공인이 홍보를 희망하는 대상에 대한 이미지 분석 결과: {visionContext}
            - 과거 우수 성과물 레퍼런스: {performersContext}

            중요 지시사항:
            1. 숏폼 영상의 자막 및 설명란 용도이므로, 띄어쓰기 포함 50자 이내, 짧고 강렬한 1~2문장으로 작성하세요.
            2. "내용 :", "마케팅 문구 :" 등 어떠한 메타 텍스트나 접두사도 절대 포함하지 마세요. 오직 실제 사용될 텍스트만 작성하세요.
            3. 홍보 텍스트는 [실시간 날씨 컨텍스트]의 기상 상황을 자연스럽게 반영하여 작성하세요.
            4. 해시태그({hashTag})의 의미만 자연스럽게 반영하되 텍스트 내에 '#' 기호나 해시태그 단어는 포함하지 마세요.
            """.trimIndent()

        return chatClient.prompt()
            .user { u ->
                u.text(template)
                    .param("weatherContext", weatherContext)
                    .param("visionContext", visionContext)
                    .param("performersContext", performersContext)
                    .param("moodTag", moodTag)
                    .param("hashTag", hashTag)
                    .param("userPrompt", userPrompt)
                    .param("uploadDay", uploadDay)
                    .param("uploadTime", uploadTime)
            }
            .call()
            .entity(MarketingContent::class.java)
    }
}
