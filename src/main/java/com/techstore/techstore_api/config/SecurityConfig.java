package com.techstore.techstore_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

   @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 1. Activation du CORS avec la bonne syntaxe ✨
        .cors(Customizer.withDefaults())
        
        // 2. Désactivation du CSRF (pour API Stateless)
        .csrf(AbstractHttpConfigurer::disable)
        
        // 3. Gestion de session sans état (JWT)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // 4. Configuration des accès (L'ordre est CRUCIAL ✨)
        .authorizeHttpRequests(auth -> auth
            // A. ACCÈS PUBLIC : SWAGGER, DOCS, ERREURS & IMAGES ✨
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/favicon.ico", "/error").permitAll()
            .requestMatchers("/uploads/**").permitAll() 

            // B. ACCÈS PUBLIC : LOGIQUE MÉTIER
            // B. ACCÈS PUBLIC : LOGIQUE MÉTIER (Indispensable pour le Guest Checkout)
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers(
    "/api/v1/products", "/api/v1/products/**", 
    "/api/v1/categories", "/api/v1/categories/**", 
    "/api/v1/shipping-zones", "/api/v1/shipping-zones/**", 
    "/api/v1/orders", "/api/v1/orders/**", 
    "/api/v1/cart", "/api/v1/cart/**"
).permitAll()
            
            // C. ACCÈS PUBLIC : REVIEWS & SETTINGS
            .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/settings").permitAll()
            
            // D. ACCÈS PRIVÉ : ADRESSES & PROFIL
            .requestMatchers("/api/v1/addresses/**").authenticated()
            .requestMatchers("/api/v1/users/me").authenticated()
            
            // E. ACCÈS PRIVÉ : ADMINISTRATION
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            
            // F. TOUT LE RESTE nécessite une authentification
            .anyRequest().authenticated()
        )
        
        // 5. Ajout du filtre JWT avant le filtre standard
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // On autorise localhost, ton domaine principal Vercel, ET tous les sous-domaines de tes déploiements Vercel ✨
    configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:5173",
        "https://techelectronique-frond-end.vercel.app",
        "https://techelectronique-front-end.vercel.app",
        "https://*-valdes-hacks-projects.vercel.app" // Autorise toutes les URLs de test Vercel
    ));
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*")); 
    configuration.setExposedHeaders(Arrays.asList("X-Session-Id", "Authorization"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}