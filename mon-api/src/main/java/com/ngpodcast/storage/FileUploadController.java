package com.ngpodcast.storage;

import com.ngpodcast.user.User;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final StorageService storageService;

    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse uploadImage(
            @AuthenticationPrincipal User user,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        String ownerFolder = user.getId() + "/content";
        return new UploadResponse(storageService.saveImage(image, ownerFolder));
    }

    public record UploadResponse(String url) {}
}
