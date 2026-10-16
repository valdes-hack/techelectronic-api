package com.techstore.techstore_api.service;

import com.techstore.techstore_api.model.AppSetting;
import com.techstore.techstore_api.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppSettingService {

    private final AppSettingRepository appSettingRepository;

    @Transactional
    @Cacheable("settings")
    public AppSetting getSettings() {
        AppSetting settings = appSettingRepository.findAll().stream().findFirst().orElseGet(() -> {
            AppSetting defaultSettings = new AppSetting();
            return appSettingRepository.save(defaultSettings);
        });

        // Auto-migration des données résiduelles (TechStore, ELECTRONIQUE) vers PEOC
        boolean modified = false;
        if (settings.getSiteName() == null || 
            settings.getSiteName().toUpperCase().contains("ELECTRONIQUE") || 
            settings.getSiteName().equalsIgnoreCase("TechStore")) {
            settings.setSiteName("PEOC (Petit Éleveur de l'Ouest Cameroun)");
            modified = true;
        }
        if (settings.getContactEmail() == null || settings.getContactEmail().toLowerCase().contains("techstore")) {
            settings.setContactEmail("contact@peoc.cm");
            modified = true;
        }
        if (settings.getContactAddress() == null || settings.getContactAddress().toLowerCase().contains("douala")) {
            settings.setContactAddress("Bafoussam, Ouest - Cameroun");
            modified = true;
        }
        if (settings.getContactPhone() == null || settings.getContactPhone().equals("+237 600 000 000")) {
            settings.setContactPhone("+237 686 669 222");
            modified = true;
        }
        if (settings.getBentoPoultryImageUrl() == null || settings.getBentoPoultryImageUrl().isBlank()) {
            settings.setBentoPoultryImageUrl("https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=1200&q=80");
            modified = true;
        }
        if (settings.getBentoIncubatorImageUrl() == null || settings.getBentoIncubatorImageUrl().isBlank()) {
            settings.setBentoIncubatorImageUrl("https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=800&q=80");
            modified = true;
        }
        if (settings.getBentoFeedImageUrl() == null || settings.getBentoFeedImageUrl().isBlank()) {
            settings.setBentoFeedImageUrl("https://images.unsplash.com/photo-1574943320219-553eb213f72d?w=800&q=80");
            modified = true;
        }
        if (settings.getBentoHealthImageUrl() == null || settings.getBentoHealthImageUrl().isBlank()) {
            settings.setBentoHealthImageUrl("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800&q=80");
            modified = true;
        }
        if (modified) {
            settings = appSettingRepository.save(settings);
        }
        return settings;
    }

    @Transactional
    @CacheEvict(value = "settings", allEntries = true)
    public AppSetting updateSettings(AppSetting newSettings) {
        AppSetting current = getSettings();
        current.setSiteName(newSettings.getSiteName());
        current.setContactEmail(newSettings.getContactEmail());
        current.setContactPhone(newSettings.getContactPhone());
        current.setContactAddress(newSettings.getContactAddress());
        if (newSettings.getLogoUrl() != null && !newSettings.getLogoUrl().isEmpty()) {
            current.setLogoUrl(newSettings.getLogoUrl());
        }
        if (newSettings.getHeroImageUrl() != null && !newSettings.getHeroImageUrl().isEmpty()) {
            current.setHeroImageUrl(newSettings.getHeroImageUrl());
        }
        if (newSettings.getHeroImagesUrls() != null) {
            if (current.getHeroImagesUrls() == null) {
                current.setHeroImagesUrls(new java.util.ArrayList<>());
            }
            current.getHeroImagesUrls().clear();
            current.getHeroImagesUrls().addAll(newSettings.getHeroImagesUrls());
        }
        current.setHeroVideoUrl(newSettings.getHeroVideoUrl());
        
        if (newSettings.getBentoPoultryImageUrl() != null) {
            current.setBentoPoultryImageUrl(newSettings.getBentoPoultryImageUrl());
        }
        if (newSettings.getBentoIncubatorImageUrl() != null) {
            current.setBentoIncubatorImageUrl(newSettings.getBentoIncubatorImageUrl());
        }
        if (newSettings.getBentoFeedImageUrl() != null) {
            current.setBentoFeedImageUrl(newSettings.getBentoFeedImageUrl());
        }
        if (newSettings.getBentoHealthImageUrl() != null) {
            current.setBentoHealthImageUrl(newSettings.getBentoHealthImageUrl());
        }

        return appSettingRepository.save(current);
    }
}
