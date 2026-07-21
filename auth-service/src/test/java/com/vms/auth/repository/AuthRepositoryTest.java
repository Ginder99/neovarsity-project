package com.vms.auth.repository;

import com.vms.auth.entity.Role;
import com.vms.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class AuthRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void enforcesUniqueEmail() {
        userRepository.saveAndFlush(buildUser());

        User duplicate = buildUser();
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User buildUser() {
        return new User("jane@example.com", "Jane Doe", "$2a$10$hash", Role.CONSUMER, true);
    }
}
