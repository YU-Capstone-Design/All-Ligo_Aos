package com.example.allligo_aos.youtube.controller

import com.example.allligo_aos.youtube.dto.YoutubeUploadRequest
import com.example.allligo_aos.youtube.dto.YoutubeUploadResponse
import com.example.allligo_aos.youtube.service.YoutubeUploadService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File

@RestController
@RequestMapping("/api/marketing")
class YoutubeController(private val youtubeUploadService: YoutubeUploadService) {


    @PostMapping("/upload")
    fun upload(@RequestBody request: YoutubeUploadRequest): ResponseEntity<YoutubeUploadResponse> {

        // 파일 불러오기
        val file = File(request.localVideoPath)

        // 파일이 존재하지 않을 때
        if(!file.exists()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 로컬 비디오 파일을 찾을 수 없습니다: ${request.localVideoPath}"
            )
        }

        // 정해진 MP4 규격을 따르지 않았을 경우
        if(!request.localVideoPath.lowercase().endsWith(".mp4")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "지원하지 않는 비디오 형식입니다. MP4 파일만 업로드 바랍니다. (입력: ${request.localVideoPath})"
            )
        }

        // 파일이 정상적으로 전송되지 않았을때
        if(file.length() == 0L){
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "비디오 파일의 크기가 0입니다. 손상된 파일일 가능성이 있습니다: ${request.localVideoPath}"
            )
        }

        // 반환형에 구문을 넣을 수 있음
        return try{
            val youtubeUrl = youtubeUploadService.uploadVideo(
                filePath = request.localVideoPath,
                title = request.title,
                description = request.description,
                tags = request.tags,
                privacyStatus = request.privacyStatus
            )
            ResponseEntity.ok(
                YoutubeUploadResponse(
                    status = "SUCCESS",
                    scheduleId = request.scheduleId,
                    youtubeUrl = youtubeUrl
                )
            )
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "YouTube 업로드 실패: ${e.message}")
        }

    }

}