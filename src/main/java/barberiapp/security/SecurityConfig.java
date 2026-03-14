package barberiapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // En producción, setea ALLOWED_ORIGINS=https://tudominio.com
    @Value("${allowed.origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOriginsRaw;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)   // JWT stateless → CSRF no aplica
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // ── Security headers ──────────────────────────────────────────────
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())          // Anti-clickjacking
                .contentTypeOptions(cto -> {})                      // No MIME sniffing
                .httpStrictTransportSecurity(hsts -> hsts           // Forzar HTTPS
                    .maxAgeInSeconds(31_536_000)
                    .includeSubDomains(true))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/barbers", "/api/barbers/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shops").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shops/*/barbers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shops/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shops/*/services").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shops/*/products").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shops/*/subscription-plans").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/appointments/booked-barbers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/barbers/*/galleries").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 12 (más seguro que el default 10)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Leer orígenes desde variable de entorno (producción vs desarrollo)
        List<String> origins = List.of(allowedOriginsRaw.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
