package pos.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

/**
 * Carga variables desde archivo .env en desarrollo
 * En producción, usa variables de entorno del sistema operativo
 */
@Configuration
public class DotEnvConfig {

    public DotEnvConfig() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            // Cargar cada variable en System.setProperty
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
            
            System.out.println("✓ Variables de entorno cargadas desde .env");
        } catch (Exception e) {
            System.out.println("ℹ No se encontró .env, usando variables de entorno del sistema");
        }
    }
}
