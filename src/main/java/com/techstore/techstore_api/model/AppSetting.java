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
    private String siteName = "PEOC (Petit Éleveur de l'Ouest Cameroun)";

    @Column(nullable = false)
    @Builder.Default
    private String contactEmail = "contact@peoc.cm";

    @Column(nullable = false)
    @Builder.Default
    private String contactPhone = "+237 686 669 222";

    @Column(nullable = false)
    @Builder.Default
    private String contactAddress = "Bafoussam, Ouest - Cameroun";

    @Column(length = 1000)
    private String logoUrl;

    @Column(length = 1000)
    private String heroImageUrl;

    @jakarta.persistence.Convert(converter = com.techstore.techstore_api.util.StringListConverter.class)
    @Column(name = "hero_images_urls", columnDefinition = "TEXT")
    @Builder.Default
    private java.util.List<String> heroImagesUrls = new java.util.ArrayList<>();

    @Column(length = 1000)
    private String heroVideoUrl;

    @Column(length = 1000)
    @Builder.Default
    private String bentoPoultryImageUrl = "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=1200&q=80";

    @Column(length = 1000)
    @Builder.Default
    private String bentoIncubatorImageUrl = "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=800&q=80";

    @Column(length = 1000)
    @Builder.Default
    private String bentoFeedImageUrl = "https://images.unsplash.com/photo-1574943320219-553eb213f72d?w=800&q=80";

    @Column(length = 1000)
    @Builder.Default
    private String bentoHealthImageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800&q=80";
}
