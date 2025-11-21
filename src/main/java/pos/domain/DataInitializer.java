package pos.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pos.domain.TableSpot;
import pos.repository.TableRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initTables(TableRepository tableRepository) {
        return args -> {
            if (tableRepository.count() == 0) {
                tableRepository.save(TableSpot.builder().code("Mesa 1").capacity(4).build());
                tableRepository.save(TableSpot.builder().code("Mesa 2").capacity(4).build());
                tableRepository.save(TableSpot.builder().code("Mesa 3").capacity(4).build());
                tableRepository.save(TableSpot.builder().code("Mesa 4").capacity(4).build());
                System.out.println("👉 Se crearon 4 mesas por defecto.");
            } else {
                System.out.println("👉 Las mesas ya existen, no se crean nuevamente.");
            }
        };
    }
}
