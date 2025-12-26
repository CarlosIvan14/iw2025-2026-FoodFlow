package pos.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.Role;
import pos.domain.User;
import pos.repository.UserRepository;

import java.util.List;

import static java.lang.Boolean.FALSE;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // defina um @Bean de BCrypt em algum @Configuration

    public User create(User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("New user must not have an id");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }

        // Codificar a senha
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Garantir que o usuário esteja ativo (mas não sobrescrever se já veio definido)
        if (user.getActive() == FALSE) {
            user.setActive(true);
        }

        // NÃO definir role como CLIENT - usar o que veio do JSON
        // user.setRole(Role.CLIENT); ← REMOVA ESTA LINHA

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found id=" + id));
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return userRepository.findAll();
    }

    public User update(Long id, User payload) {
        User u = get(id);

        // se trocar email, verifique unicidade
        if (!u.getEmail().equalsIgnoreCase(payload.getEmail()) &&
                userRepository.existsByEmail(payload.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + payload.getEmail());
        }

        u.setName(payload.getName());
        u.setEmail(payload.getEmail());

        if (payload.getPasswordHash() != null && !payload.getPasswordHash().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(payload.getPasswordHash()));
        }

        if (payload.getRole() != null) u.setRole(payload.getRole());
        u.setActive(payload.getActive());

        // como u é gerenciado pelo JPA dentro da @Transactional, o flush ocorre automaticamente
        return u;
    }

    public void delete(Long id) {
        User u = get(id);
        userRepository.delete(u);
    }

    public User setActive(Long id, boolean active) {
        User u = get(id);
        u.setActive(active);
        return u;
    }


}

