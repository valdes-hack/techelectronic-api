package com.techstore.techstore_api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired(required = false)
    private Cloudinary cloudinary;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${cloudinary.enabled:true}")
    private boolean cloudinaryEnabled;

    public String storeFile(MultipartFile file) {
        try {
            // Utiliser Cloudinary si activé et disponible
            if (cloudinaryEnabled && cloudinary != null) {
                return storeFileOnCloudinary(file);
            }

            // Sinon, stockage local (fallback)
            return storeFileLocally(file);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de stocker le fichier", e);
        }
    }

    private String storeFileOnCloudinary(MultipartFile file) throws IOException {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", "products/" + fileName,
                    "folder", "products",
                    "resource_type", "auto"
                )
            );

            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Erreur upload Cloudinary: " + e.getMessage(), e);
        }
    }

    private String storeFileLocally(MultipartFile file) throws IOException {
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
    }
}