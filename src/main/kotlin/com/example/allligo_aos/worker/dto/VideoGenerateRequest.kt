package com.example.allligo_aos.worker.dto

data class VideoGenerateRequest(
    val imageFilenames: List<String>,
    val marketingText: String,
    val secondsPerImage: Double = 3.0,
    val transitionDuration: Double = 0.7
)
