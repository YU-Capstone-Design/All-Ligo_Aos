package com.example.allligo_aos.worker.service

import com.example.allligo_aos.worker.dto.ImageGenerateRequest
import com.example.allligo_aos.worker.dto.ImageGenerateResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody
import java.time.Duration


@Service
class ImageWorkerService(
    restClientBuilder: RestClient.Builder,
    @Value("\${worker.base-url}") workerBaseUrl: String,
) {

    private val restClient: RestClient = restClientBuilder
        .baseUrl(workerBaseUrl) // 여기다가 요청해야함
        .requestFactory( // 커스텀 타임아웃 설정
            SimpleClientHttpRequestFactory().apply { // apply 앞에 있는 객체에 대해 설정을 완료한 후 그대로 객체 자신을 리턴한다.
                setConnectTimeout(Duration.ofSeconds(5).toMillis().toInt())
                setReadTimeout(Duration.ofMinutes(2).toMillis().toInt())
            }
        )
        .build()

    /**
     * 이미지 생성 메소드
     *
     */
    fun generateImage(request: ImageGenerateRequest): ImageGenerateResponse {
        return restClient.post()
            .uri("/generate/image")
            .body(request)
            .retrieve()
            .requiredBody<ImageGenerateResponse>()
    }

}