package com.example.allligo_aos.system.controller

import com.example.allligo_aos.system.service.SystemStatusService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/system")
class SystemStatusController(private val systemStatusService: SystemStatusService) {

    @GetMapping("/status")
    suspend fun getStatus(): ResponseEntity<*> =
        ResponseEntity.ok(systemStatusService.getSystemStatus())

}