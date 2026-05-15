package com.medibook.auth.service;

import com.medibook.auth.entity.User;
import com.medibook.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void suspendUser(Long id) {
        User user = getUserById(id);
        if ("admin@medibook.com".equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Cannot suspend the main administrator account.");
        }
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if ("admin@medibook.com".equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Cannot delete the main administrator account.");
        }
        userRepository.deleteById(id);
    }
}
