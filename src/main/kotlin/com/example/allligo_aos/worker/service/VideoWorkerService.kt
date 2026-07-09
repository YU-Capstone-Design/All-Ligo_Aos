package com.example.allligo_aos.worker.service

import com.example.allligo_aos.worker.dto.VideoGenerateRequest
import com.example.allligo_aos.worker.dto.VideoGenerateResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody
import java.time.Duration

@Service
class VideoWorkerService(
    restClientBuilder: RestClient.Builder,
    @Value("\${worker.base-url}") workerBaseUrl: String
) {

    private val restClient: RestClient = restClientBuilder
        .baseUrl(workerBaseUrl)
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(Duration.ofSeconds(5).toMillis().toInt())
                setReadTimeout(Duration.ofMinutes(5).toMillis().toInt())
            }
        )
        .build()


    fun generateVideo(request: VideoGenerateRequest): VideoGenerateResponse {
        return restClient.post()
            .uri("/generate/video")
            .body(request)
            .retrieve()
            .requiredBody<VideoGenerateResponse>()
    }

}