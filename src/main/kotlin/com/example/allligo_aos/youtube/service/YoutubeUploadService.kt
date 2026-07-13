package com.example.allligo_aos.youtube.service

import com.example.allligo_aos.youtube.dto.YoutubeToken
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoSnippet
import com.google.api.services.youtube.model.VideoStatus
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.UserCredentials
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.io.File

@Service
// 생성자 파라미터 2개: Jackson ObjectMapper(스프링이 자동으로 만들어서 주입해줌), token.json 경로(application.yaml에서 주입)
class YoutubeUploadService(
    private val objectMapper: ObjectMapper,
    @Value("\${youtube.token-path}") private val tokenPath: String,
) {

    // by lazy: 이 프로퍼티(youtube)에 처음 접근하는 순간에만 딱 한 번 아래 블록을 실행하고, 그 결과를 캐싱해서 재사용
    // 스프링 빈이 만들어지는 시점(앱 기동 시점)엔 아직 필요 없는 무거운 작업(파일 읽기 + OAuth 클라이언트 생성)이라 미뤄둠
    private val youtube: YouTube by lazy {

        // tokenPath 경로의 JSON 파일(token.json)을 읽어서 YoutubeToken 데이터 클래스로 역직렬화
        val token = objectMapper.readValue(File(tokenPath), YoutubeToken::class.java)

        // 읽어온 clientId/clientSecret/refreshToken으로 구글 인증 자격증명(Credentials) 객체 생성
        // 이 객체가 나중에 access token 만료 시 refreshToken으로 자동 갱신을 처리해줌
        val credentials = UserCredentials.newBuilder()
            .setClientId(token.clientId)
            .setClientSecret(token.clientSecret)
            .setRefreshToken(token.refreshToken)
            .build()

        // 실제 YouTube API 클라이언트(YouTube 객체) 생성
        // NetHttpTransport(): 실제 HTTP 통신을 담당하는 전송 계층 구현체
        // GsonFactory: 요청/응답 JSON 파싱에 쓸 JSON 라이브러리로 Gson을 지정
        // HttpCredentialsAdapter(credentials): 위에서 만든 인증 정보를, 매 요청마다 Authorization 헤더에 실어주는 역할로 감싸줌
        YouTube.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("All-Ligo") // 구글 API 콘솔에 표시될 애플리케이션 이름(임의로 지정 가능)
            .build()
    }

    // 실제 영상을 유튜브에 업로드하는 함수. 성공하면 시청 가능한 URL을 리턴
    fun uploadVideo(
        filePath: String,       // 로컬에 저장된 mp4 파일 경로
        title: String,          // 영상 제목
        description: String,    // 영상 설명
        tags: List<String>,     // 영상 태그 목록
        privacyStatus: String   // 공개 상태 (public/unlisted/private)
    ): String {

        // 영상의 메타데이터(제목/설명/태그/카테고리)를 담을 객체 생성
        // .apply{}: 새로 만든 VideoSnippet 객체를 this로 두고, 블록 안에서 프로퍼티들을 설정한 뒤, 그 객체 자신을 그대로 리턴
        val snippet = VideoSnippet().apply {
            this.title = title.take(100) // 앞 100글자만 가져옴 (유튜브 제목 길이 제한 100자)
            this.description = description
            this.tags = tags
            categoryId = "22" // 유튜브 카테고리 ID, "22"는 People & Blogs
        }

        // 공개 상태 등 업로드 관련 상태 정보를 담을 객체 생성
        val status = VideoStatus().apply {
            this.privacyStatus = privacyStatus
            selfDeclaredMadeForKids = false // 아동용 영상 아님으로 명시
        }

        // 위에서 만든 snippet, status를 하나의 Video 객체로 합침 (실제 업로드 요청 바디에 해당)
        val video = Video().apply {
            this.snippet = snippet
            this.status = status
        }

        // 실제 영상 파일 자체(바이너리 데이터)를 요청에 실을 수 있는 형태로 감쌈, mp4 mimeType 지정
        val mediaContent = FileContent("video/mp4", File(filePath))

        // "snippet","status" 파트를 포함해서 video 메타데이터 + 실제 파일(mediaContent)을 업로드 요청으로 생성하고 즉시 실행
        // .execute()가 실제로 네트워크 요청을 보내고, 완료될 때까지 이 지점에서 블로킹됨
        val response = youtube.videos()
            .insert(listOf("snippet", "status"), video, mediaContent)
            .execute()

        // 업로드 응답에서 videoId 추출, 혹시라도 없으면(비정상 상황) 예외를 던짐
        val videoId = response.id
            ?: throw IllegalStateException("YouTube 업로드는 완료됐지만 videoId를 받지 못했습니다.")

        // 최종적으로 시청 가능한 숏폼 URL 형태로 조립해서 리턴
        return "https://youtube.com/shorts/$videoId"
    }
}