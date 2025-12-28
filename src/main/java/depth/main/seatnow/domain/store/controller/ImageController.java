package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.infrastructure.external.s3.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final S3UploadService s3UploadService;

    @PostMapping("/upload")
    public ApiResponse<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String imageUrl = s3UploadService.uploadFile(file);
        return ApiResponse.ok(imageUrl);
    }
}
