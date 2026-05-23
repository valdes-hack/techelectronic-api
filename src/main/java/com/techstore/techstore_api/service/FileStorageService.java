package com.techstore.techstore_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // 1. Créer le dossier s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Générer un nom unique (ex: a1b2-c3d4-image.jpg)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 3. Copier le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName; // On retourne le nom pour l'enregistrer en BD
        } catch (IOException e) {
            throw new RuntimeException("Impossible de stocker le fichier", e);
        }
    }
}