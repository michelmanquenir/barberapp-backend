package barberiapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permitir todas las rutas
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173") // Permitir el origin del frontend
                                                                                  // Vite
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Metodos permitidos
                .allowedHeaders("*") // Permitir todos los headers
                .allowCredentials(true); // Permitir credenciales como cookies o auth headers
    }
}
