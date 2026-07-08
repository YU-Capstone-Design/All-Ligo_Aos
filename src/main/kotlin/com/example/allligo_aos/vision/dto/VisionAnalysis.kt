package com.example.allligo_aos.vision.dto

data class VisionAnalysis(
    val objects: List<String>,
    val mood: List<String>,
    val colors: List<String>
)
