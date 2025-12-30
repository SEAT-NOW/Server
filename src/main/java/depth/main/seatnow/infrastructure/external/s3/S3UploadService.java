package depth.main.seatnow.infrastructure.external.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 1. 최초 업로드: 무조건 temp/ 폴더로 들어간다.
    public String uploadFile(MultipartFile file) {
        // 파일명 중복 방지를 위한 고유값 생성
        String fileName = "temp/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // S3Client(v2)를 이용한 업로드
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 생성된 파일의 S3 URL 반환
            return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucket, fileName);

        } catch (IOException e) {
            throw new RuntimeException("S3 사진 업로드 중 에러가 발생했습니다.", e);
        }
    }

    // 2. 파일 확정: 가입 버튼 클릭 시 temp -> permanent로 이동시킨다.
    public String confirmImage(String tempUrl) {
        if (tempUrl == null || !tempUrl.contains("temp/")) return tempUrl;

        String tempKey = tempUrl.substring(tempUrl.lastIndexOf(".com/") + 5);
        String permanentKey = tempKey.replace("temp/", "permanent/");

        try {
            // S3 내에서 복사
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(tempKey)
                    .destinationBucket(bucket)
                    .destinationKey(permanentKey)
                    .build());

            // 원본 temp 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(tempKey)
                    .build());

            return tempUrl.replace(tempKey, permanentKey);
        } catch (Exception e) {
            return tempUrl; // 실패 시 원래 URL 반환
        }
    }
}
