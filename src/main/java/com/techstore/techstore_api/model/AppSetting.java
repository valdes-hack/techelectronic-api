package com.techstore.techstore_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private String siteName = "TechStore";

    @Column(nullable = false)
    @Builder.Default
    private String contactEmail = "contact@techstore.cm";

    @Column(nullable = false)
    @Builder.Default
    private String contactPhone = "+237 600 000 000";

    @Column(nullable = false)
    @Builder.Default
    private String contactAddress = "Douala, Cameroun";

    @Column(length = 1000)
    private String logoUrl;

    @Column(length = 1000)
    private String heroImageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_settings_hero_images", joinColumns = @JoinColumn(name = "app_setting_id"))
    @Column(name = "image_url", length = 1000)
    @Builder.Default
    private java.util.List<String> heroImagesUrls = new java.util.ArrayList<>();

    @Column(length = 1000)
    private String heroVideoUrl;
}
