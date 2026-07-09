package com.example.allligo_aos.worker.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

@Service
class S3UploadService(
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.s3.bucket}") private val bucketName: String,
    @Value("\${aws.s3.base-url}") private val baseUrl: String
) {

    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(region))
        .build()

    fun uploadVideo(filePath: String, objectName: String? = null): String {
        val file = File(filePath);
        val key = objectName ?: file.name // 오브젝트 이름이 null이면 파일 이름을 쓰기

        val request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType("video/mp4")
            .build()

        s3Client.putObject(request, RequestBody.fromFile(file))

        // 리턴문의 조건문
        return if (baseUrl.isNotBlank()) {
            // 문자열 끝에 붙은 '/' 제거
            "${baseUrl.trimEnd('/')}/$key"
        } else if (region == "us-east-1") {
            "https://$bucketName.s3.amazonaws.com/$key"
        } else {
            "https://$bucketName.s3.$region.amazonaws.com/$key"
        }
    }

}