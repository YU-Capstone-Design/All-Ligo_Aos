package com.example.allligo_aos.system.service

import com.example.allligo_aos.system.dto.GpuStatus
import com.example.allligo_aos.system.dto.SystemStatusResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@Service
class SystemStatusService {

    // 클래스 전체가 공유하는 값
    // Java에서는 private static final int MAX_CONCURRENT_JOBS = 2;
    companion object {
        // 컴파일 시간대에 값이 정해지는건 const val
        // 런타임에 정해져도 되면 val
        private const val MAX_CONCURRENT_JOBS = 2
    }

    // 여러 스레드가 동시에 값을 건드려도 값이 꼬이지 않게 보장해주는 정수 래퍼 클래스 - Java랑 동일한거 사용
    private val activeJobs = AtomicInteger(0)

    /**
     * 디스크 여유공간 체크 함수
     */
    private fun getDiskFreeMb(): Long {
        // 바이트 단위의 Long 값을 줌
        // Java의 new File("static").getUsableSpace(); 랑 동일함
        val free: Long = File("static").usableSpace
        // 메가바이트로 변환
        return free / (1024 * 1024)
    }

    /**
     * GPU 상태 가져오는 함수
     */
    private fun getGpuStatus(): GpuStatus? {
        // 반환할때 즉시 try-catch를 사용 가능
        return try {
            // 프로세스 빌더는 자바에도 존재함.
            val process = ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=utilization.gpu,utilization.memory,memory.used,memory.total",
                "--format=csv,noheader,nounits"
            ).start()

            // 프로세스가 화면에 출력한 내용을 입력받는 스트림을 버퍼리더로 읽어 하나의 String으로 변환하고 끝의 개행문자나 공백 제거
            val output = process.inputStream.bufferedReader().readText().trim()
            // 프로세스가 완전히 종료될때까지 대기
            process.waitFor()

            // 쉼표 기준으로 잘라서 리스트로, 앞뒤 공백 제거
            val parts = output.split(",").map{ it.trim() }


            if(parts.size >= 4){
                // Int형으로 변환
                // Java의 parts.get(0) 와 같은데 조금 더 배열처럼 가져올 수 있음
                GpuStatus(
                    gpuUtil = parts[0].toInt(),
                    gpuMemUtil = parts[1].toInt(),
                    gpuMemUsedMb = parts[2].toInt(),
                    gpuMemTotalMb = parts[3].toInt(),
                )
            } else {
                null
            }
        } catch (e: Exception){
            null
        }
    }

    /**
     * 시스템 정보 불러오기
     *
     * suspend fun은 중간에 멈췄다가 재개될 수 있는 함수라는 표식
     * 코루틴 관련 함수는 해당 함수 안에서만 호출 가능
     *
     * coroutineScope{} 이 블록 안에서 만든 자식 코루틴들이 모두 끝나야 이 블록도 끝난다고 보장해주는 함수
     */
    suspend fun getSystemStatus(): SystemStatusResponse = coroutineScope {

        // async는 블록안에 코드를 즉시 백그라운드로 던지고 결과를 나중에 받을 수 있는 Deferred 객체를 바로 리턴.
        val diskFreeDeferred = async { getDiskFreeMb() }
        val gpuStatusDeferred = async { getGpuStatus() }

        // await는 Deferred가 들고있는 실제 값을 꺼냄. 두 값이 다 오면 다음 함수 줄로 넘어감
        val diskFreeMb = diskFreeDeferred.await()
        val gpuStatus = gpuStatusDeferred.await()

        // GPU 정보가 존재하고, 사용량이 90%이상이거나 메모리 점유가 80%이상이면 바쁜 상태를 저장
        val gpuBusy = gpuStatus != null && (gpuStatus.gpuUtil >= 90 || gpuStatus.gpuMemUtil >= 80)

        // 지금 진행 중인 작업 수가 최대치(2개) 이상, 디스크 여유공간이 1000MB이하, GPU 바쁨 셋중 하나라도 참이면 바쁜상태.
        val currentStatus = if (activeJobs.get() >= MAX_CONCURRENT_JOBS || diskFreeMb <= 1000 || gpuBusy) {
            "busy"
        } else {
            "available"
        }

        // coroutine함수가 마지막 줄 값인 SystemStatusResponse를 리턴
        SystemStatusResponse(
            status = currentStatus, // 현재 상태
            activeJobs = activeJobs.get(), // 현재 작동중인 작업 개수
            maxConcurrentJobs = MAX_CONCURRENT_JOBS, // 최대 작업 허용 개수
            diskFreeMb = diskFreeMb, // 스토리지 여유 공간 MB
            gpu = gpuStatus // GPU 사용량
        )
    }
}