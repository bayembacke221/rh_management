package sn.bmbacke.rh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.CommandLineRunner;
import sn.bmbacke.rh.entity.Role;
import sn.bmbacke.rh.repository.RoleRepository;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class RhManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhManagementApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                roleRepository.save(Role.builder().name("ADMIN").build());
            }
            if (roleRepository.findByName("HR").isEmpty()) {
                roleRepository.save(Role.builder().name("HR").build());
            }
            if (roleRepository.findByName("MANAGER").isEmpty()) {
                roleRepository.save(Role.builder().name("MANAGER").build());
            }
        };
    }
}
