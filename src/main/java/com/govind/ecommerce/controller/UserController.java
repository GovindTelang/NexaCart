package com.govind.ecommerce.controller;

import com.govind.ecommerce.model.User;
import com.govind.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody User user) {
        return userService.loginUser(
                user.getEmail(),
                user.getPassword()
        );
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}