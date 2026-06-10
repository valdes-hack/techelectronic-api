package com.techstore.techstore_api.service;

import com.techstore.techstore_api.model.AppSetting;
import com.techstore.techstore_api.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppSettingService {

    private final AppSettingRepository appSettingRepository;

    public AppSetting getSettings() {
        return appSettingRepository.findAll().stream().findFirst().orElseGet(() -> {
            AppSetting defaultSettings = new AppSetting();
            return appSettingRepository.save(defaultSettings);
        });
    }

    @Transactional
    public AppSetting updateSettings(AppSetting newSettings) {
        AppSetting current = getSettings();
        current.setSiteName(newSettings.getSiteName());
        current.setContactEmail(newSettings.getContactEmail());
        current.setContactPhone(newSettings.getContactPhone());
        current.setContactAddress(newSettings.getContactAddress());
        if (newSettings.getLogoUrl() != null && !newSettings.getLogoUrl().isEmpty()) {
            current.setLogoUrl(newSettings.getLogoUrl());
        }
        return appSettingRepository.save(current);
    }
}
