package sn.bmbacke.rh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Role;
import sn.bmbacke.rh.entity.User;
import sn.bmbacke.rh.entity.enums.Gender;
import sn.bmbacke.rh.entity.enums.Status;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.RoleRepository;
import sn.bmbacke.rh.repository.UserRepository;

import java.util.List;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class RhManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhManagementApplication.class, args);
    }

    private Role createRoleIfNotExists(RoleRepository roleRepository, String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository,
                                    UserRepository userRepository,
                                    EmployeeRepository employeeRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            createRoleIfNotExists(roleRepository, "HR");
            createRoleIfNotExists(roleRepository, "MANAGER");
            createRoleIfNotExists(roleRepository, "EMPLOYEE");

            Role adminRole = createRoleIfNotExists(roleRepository, "ADMIN");

            if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
                User adminUser = User.builder()
                        .email("admin@admin.com")
                        .username("admin@admin.com")
                        .password(passwordEncoder.encode("admin"))
                        .roles(List.of(adminRole))
                        .enabled(true)
                        .createdBy("System")
                        .build();

                userRepository.save(adminUser);

                Employee adminEmployee = Employee.builder()
                        .firstName("Admin")
                        .lastName("Admin")
                        .phone("777777777")
                        .gender(Gender.MALE)
                        .user(adminUser)
                        .status(Status.ACTIVE)
                        .createdBy("System")
                        .build();

                employeeRepository.save(adminEmployee);

                adminUser.setEmployee(adminEmployee);
                userRepository.save(adminUser);
            }
        };
    }
}
