package pos.domain;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pos.domain.TableSpot;
import pos.repository.TableRepository;

@Configuration("domainDataInitializer")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(TableRepository tableRepository, pos.repository.UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            if (tableRepository.count() == 0) {
                tableRepository.save(TableSpot.builder().code("Mesa 1").capacity(4).build());
                tableRepository.save(TableSpot.builder().code("Mesa 2").capacity(4).build());
                tableRepository.save(TableSpot.builder().code("Mesa 3").capacity(4).build());
                tableRepository.save(TableSpot.builder().code("Mesa 4").capacity(4).build());
                System.out.println("👉 Se crearon 4 mesas por defecto.");
            }

            if (userRepository.count() == 0) {
                userRepository.save(pos.domain.User.builder()
                    .email("german@test.com")
                    .name("german del rio")
                    .passwordHash(passwordEncoder.encode("password"))
                    .role(pos.domain.Role.ADMIN)
                    .active(true)
                    .build());
                System.out.println("👉 Usuario 'german del rio' creado (email: german@test.com, pass: password).");
            }
        };
    }
}
