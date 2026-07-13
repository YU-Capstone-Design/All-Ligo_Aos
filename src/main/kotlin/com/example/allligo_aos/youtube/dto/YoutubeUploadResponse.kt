package com.example.allligo_aos.youtube.dto

data class YoutubeUploadResponse(
    val status: String,
    val scheduleId: String? = null,
    val youtubeUrl: String? = null,
    val error: String? = null,
)
