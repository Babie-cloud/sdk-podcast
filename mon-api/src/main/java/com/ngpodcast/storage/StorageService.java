package com.ngpodcast.storage;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Value("${storage.local.path:uploads}")
    private String uploadPath;

    @Value("${storage.base-url:http://localhost:8080/files}")
    private String baseUrl;

    public String saveAudio(MultipartFile file, String podcastId) throws IOException {
        return save(file, "audio/" + podcastId);
    }

    public String saveImage(MultipartFile file, String folder) throws IOException {
        return save(file, "images/" + folder);
    }

    public void delete(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        String prefix = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        if (!fileUrl.startsWith(prefix)) {
            log.warn(
                    "Suppression ignoree : URL fichier ne correspond pas a storage.base-url (prefix={}) url={}",
                    prefix,
                    fileUrl);
            return;
        }
        String relativePath = fileUrl.substring(prefix.length());
        if (relativePath.isEmpty() || relativePath.contains("..")) {
            throw new SecurityException("Chemin de fichier non autorise.");
        }
        Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path target = base.resolve(relativePath).normalize();
        if (!target.startsWith(base)) {
            throw new SecurityException("Chemin de fichier non autorise.");
        }
        Files.deleteIfExists(target);
    }

    private String save(MultipartFile file, String subfolder) throws IOException {
        Path dir = Paths.get(uploadPath, subfolder);
        Files.createDirectories(dir);

        String extension = getExtension(file.getOriginalFilename());
        String filename = java.util.UUID.randomUUID() + "." + extension;
        Path targetPath = dir.resolve(filename);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/" + subfolder + "/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
