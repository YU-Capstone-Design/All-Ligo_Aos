package com.example.allligo_aos.marketing.record

// Java의 record와 정확히 대응되는 개념
data class MarketingContent(
    val text: String,
    val imagePrompts: List<String>
)
