package com.ngpodcast.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${storage.local.path:uploads}")
    private String uploadPath;

    @Value("${storage.base-url:http://localhost:8080/files}")
    private String baseUrl;

    // ─── Sauvegarder un fichier audio ─────────────────────────
    public String saveAudio(MultipartFile file, String podcastId) throws IOException {
        return save(file, "audio/" + podcastId);
    }

    // ─── Sauvegarder une image de couverture ──────────────────
    public String saveImage(MultipartFile file, String folder) throws IOException {
        return save(file, "images/" + folder);
    }

    // ─── Supprimer un fichier ─────────────────────────────────
    public void delete(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        String relativePath = fileUrl.replace(baseUrl + "/", "");
        Path path = Paths.get(uploadPath).resolve(relativePath);
        Files.deleteIfExists(path);
    }

    // ─── Methode interne ──────────────────────────────────────
    private String save(MultipartFile file, String subfolder) throws IOException {
        // Créer le dossier si necessaire
        Path dir = Paths.get(uploadPath, subfolder);
        Files.createDirectories(dir);

        // Generer un nom unique
        String extension  = getExtension(file.getOriginalFilename());
        String filename   = UUID.randomUUID() + "." + extension;
        Path   targetPath = dir.resolve(filename);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner l'URL publique
        return baseUrl + "/" + subfolder + "/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}