package com.example.allligo_aos.vision.controller

import com.example.allligo_aos.vision.service.VisionAnalysisService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/vision")
class VisionController(private val visionAnalysisService: VisionAnalysisService) {

    @PostMapping("/analyze")
    fun analysis(@RequestParam("image") file: MultipartFile): ResponseEntity<*> {
        return ResponseEntity.ok(visionAnalysisService.analysis(file))
    }
}
