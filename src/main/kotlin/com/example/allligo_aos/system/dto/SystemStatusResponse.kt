package com.example.allligo_aos.system.dto

data class SystemStatusResponse(
    val status: String,
    val activeJobs: Int,
    val maxConcurrentJobs: Int,
    val diskFreeMb: Long,
    val gpu: GpuStatus? // null 허용
)
