package com.example.allligo_aos.youtube.dto

data class YoutubeUploadRequest(
    val scheduleId: String? = null,
    val localVideoPath: String,
    val title: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val privacyStatus: String = "unlisted"
)
