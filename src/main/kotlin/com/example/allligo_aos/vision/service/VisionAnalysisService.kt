package com.example.allligo_aos.vision.service

import com.example.allligo_aos.vision.record.VisionAnalysis
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import org.springframework.web.multipart.MultipartFile

@Service
class VisionAnalysisService(chatClientBuilder: ChatClient.Builder) {

    private val chatClient: ChatClient = chatClientBuilder.build()

    // llava:13b는 정식 JSON 스키마 구조화 출력을 안정적으로 못 따라감(스키마 자체를 되뇌거나 타입 불일치 발생)
    // → 원본 파이썬처럼 라벨 텍스트로 받아서 수동 파싱하는 방식으로 대체

    /**
     * 이미지를 분석하는 메소드
     *
     * @param image 분석할 이미지
     * @return 비전 분석 결과
     */
    fun analysis(image: MultipartFile): VisionAnalysis {
        val promptText = """
            You are an expert marketing analyst. Look at this image and extract key elements for an advertisement.
            Be highly specific about the objects and environment in the image.
            Provide your analysis exactly in this format:
            Objects: [highly specific main objects separated by comma]
            Mood: [emotional mood and vibe separated by comma]
            Colors: [dominant colors separated by comma]

            """.trimIndent()

        val rawText = chatClient.prompt()
            .user { u ->
                u.text(promptText)
                    .media(MimeTypeUtils.parseMimeType(image.contentType ?: "application/octet-stream"), image.resource)
            }
            .options(OllamaChatOptions.builder().model("llava:13b"))
            .call()
            .content()

        // 로텍스트를 반환하되, null일 경우에 빈 문자열 반환. 왼쪽이 null이면 오른쪽 값을 쓰는 것.
        return parseVisionAnalysis(rawText ?: "")
    }

    /**
     * 구조화 출력 불안정으로 인한 직접 파싱하는 로직
     *
     * @param text 응답 받은 텍스트
     * @return 정제된 비전 분석 결과
     */
    private fun parseVisionAnalysis(text: String): VisionAnalysis {
        // 빈 리스트 할당
        var objects: List<String> = emptyList()
        var mood: List<String> = emptyList()
        var colors: List<String> = emptyList()

        // 줄바꿈 기준으로 가져오기. for-each
        for (line in text.split("\n")) {
            val trimmedLine = line.trim()
            when {
                // when 문으로 if-else를 표현. 화살표로 조건 이후 실행을 적음
                // 앞에 있는 문자열부분을 제거하는 메소드가 별도로 존재
                trimmedLine.startsWith("Objects:") -> objects = splitList(trimmedLine.removePrefix("Objects:"))
                trimmedLine.startsWith("Mood:") -> mood = splitList(trimmedLine.removePrefix("Mood:"))
                trimmedLine.startsWith("Colors:") -> colors = splitList(trimmedLine.removePrefix("Colors:"))
            }
        }
        return VisionAnalysis(objects, mood, colors)
    }

    private fun splitList(raw: String): List<String> {
        // 스트림 활용방식이 조금 다른데 이건 이후에 교재로 공부할것.
        return raw.split(",") // 쉼표 구분자로 나누기
            .map { it.trim() } // 앞뒤 공백 제거
            .filter { it.isNotEmpty() } // 빈 문자열이 아닌거만 필터링
    }
}
