package com.example.allligo_aos.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun allLigoOpenApi(): OpenAPI {
        // 클래스() 자체가 생성자 호출이라 자바와 다르게 new 키워드를 쓰지 않고 호출
        return OpenAPI()
            .info(
                Info()
                    .title("All-Ligo AI Marketing Agent API")
                    .description(
                        "FastAPI에서 Spring AI로 마이그레이션 중인 소상공인 마케팅 콘텐츠 생성 API 문서입니다. " +
                            "현재 마이그레이션 진행 상황을 확인하는 용도로도 사용합니다."
                    )
                    .version("v0.1 (migration in progress)")
            )
    }
}
