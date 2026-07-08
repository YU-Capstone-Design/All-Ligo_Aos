package com.example.allligo_aos.system.dto

data class GpuStatus(
    val gpuUtil: Int,
    val gpuMemUtil: Int,
    val gpuMemUsedMb: Int,
    val gpuMemTotalMb: Int
)
