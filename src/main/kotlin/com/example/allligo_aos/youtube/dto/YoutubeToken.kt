package com.example.allligo_aos.youtube.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class YoutubeToken(
    @param:JsonProperty("client_id") val clientId: String,
    @param:JsonProperty("client_secret") val clientSecret: String,
    @param:JsonProperty("refresh_token") val refreshToken: String,
)
