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

        return parseVisionAnalysis(rawText ?: "")
    }

    /**
     * 구조화 출력 불안정으로 인한 직접 파싱하는 로직
     *
     * @param text 응답 받은 텍스트
     * @return 정제된 비전 분석 결과
     */
    private fun parseVisionAnalysis(text: String): VisionAnalysis {
        var objects: List<String> = emptyList()
        var mood: List<String> = emptyList()
        var colors: List<String> = emptyList()

        for (line in text.split("\n")) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("Objects:") -> objects = splitList(trimmedLine.removePrefix("Objects:"))
                trimmedLine.startsWith("Mood:") -> mood = splitList(trimmedLine.removePrefix("Mood:"))
                trimmedLine.startsWith("Colors:") -> colors = splitList(trimmedLine.removePrefix("Colors:"))
            }
        }
        return VisionAnalysis(objects, mood, colors)
    }

    private fun splitList(raw: String): List<String> {
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
