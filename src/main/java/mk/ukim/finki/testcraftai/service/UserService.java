package mk.ukim.finki.testcraftai.service;

import mk.ukim.finki.testcraftai.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(String username, String password, String email, User.Role role);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
}