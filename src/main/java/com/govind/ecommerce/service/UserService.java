package com.govind.ecommerce.service;

import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.UserRepository;
import com.govind.ecommerce.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return jwtService.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );
        }

        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}