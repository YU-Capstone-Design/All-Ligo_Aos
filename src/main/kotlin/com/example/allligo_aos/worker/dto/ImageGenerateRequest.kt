package com.example.allligo_aos.worker.dto

data class ImageGenerateRequest(
    val prompt: String,
    val negativePrompt: String? = null,
    val width: Int = 768,
    val height: Int = 1344,
    val numInferenceSteps: Int? = null,
    val guidanceScale: Double? = null
)
