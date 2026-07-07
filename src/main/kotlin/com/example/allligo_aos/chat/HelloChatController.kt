package com.example.allligo_aos.chat

import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloChatController(chatClientBuilder: ChatClient.Builder) {

    private val chatClient: ChatClient = chatClientBuilder.build()

    @GetMapping("/api/hello-ai")
    fun hello(@RequestParam(defaultValue = "안녕 너는 누구야?") message: String): String? {
        return chatClient.prompt()
            .user(message)
            .call()
            .content()
    }
}
