package com.blog.service;

import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = findUserByEmail(email);
        return convertToDTO(user);
    }

    @Transactional
    public User createOrUpdateGoogleUser(String googleId, String name, String email, String pictureUrl) {
        User user = userRepository.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            // Check if user with same email exists
            user = userRepository.findByEmail(email).orElse(new User());
            user.setGoogleId(googleId);
        }

        user.setName(name);
        user.setEmail(email);
        user.setPictureUrl(pictureUrl);

        // If first user, make them admin
        if (userRepository.count() == 0) {
            user.setRole("ADMIN");
        } else if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPictureUrl(),
                user.getRole()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findUserByEmail(email);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "", // No password as we're using Google Auth
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
