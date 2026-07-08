package com.example.allligo_aos.chat

import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
// Kotlin의 기본 가시성은 public. 생략가능
// 클래스 선언 자체에 생성자 파라미터를 바로 작성할 수 있음
class HelloChatController(chatClientBuilder: ChatClient.Builder) {

    // 위에 생성자 파라미터가 정의되어있으므로 거기서 바로 .build() 가져와 붙이기
    private val chatClient: ChatClient = chatClientBuilder.build()

    /**
     * 파라미터는 notnull, 기본값이 존재함
     * 반환형은 null을 허용하는 String 자료형
     */
    @GetMapping("/api/hello-ai")
    fun hello(@RequestParam(defaultValue = "안녕 너는 누구야?") message: String): String? {
        // Java랑 완전 동일함
        return chatClient.prompt()
            .user(message)
            .call()
            .content()
    }
}
