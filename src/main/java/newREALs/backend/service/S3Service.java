package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.repository.SubInterestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 로컬 파일을 S3에 업로드
     * @param file 업로드할 파일 객체
     * @param folder S3 버킷 내 저장 폴더 (선택)
     * @return 업로드된 파일의 S3 URL
     */
    public String uploadReportFile(File file, String folder) {
        try {
            // S3에 저장할 파일 이름
            String fileName = (folder != null ? folder + "/" : "") + UUID.randomUUID() + "_" + file.getName();

            // 파일 MIME 타입 확인
            String contentType = Files.probeContentType(file.toPath());

            // S3 업로드 요청
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromFile(file) // 파일 객체를 읽어서 업로드
            );

            // S3 URL 반환
            return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 로컬 파일 삭제 (임시 파일 관리)
     * @param file 삭제할 파일 객체
     */
    public void deleteLocalFile(File file) {
        if (file.exists() && !file.delete()) {
            System.err.println("로컬 파일 삭제 실패: " + file.getAbsolutePath());
        }
    }

    public String uploadImageFile(MultipartFile file) {
        try {
            // 파일명을 UUID로 고유하게 생성
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // S3 업로드 요청
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()) // MultipartFile 데이터를 바이트로 변환
            );

            // S3 URL 반환
            return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
}
