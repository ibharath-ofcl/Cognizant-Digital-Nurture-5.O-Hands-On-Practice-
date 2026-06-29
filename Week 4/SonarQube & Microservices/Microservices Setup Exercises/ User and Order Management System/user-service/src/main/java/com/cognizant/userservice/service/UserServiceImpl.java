package com.cognizant.userservice.service;

import com.cognizant.userservice.dto.UserDto;
import com.cognizant.userservice.entity.User;
import com.cognizant.userservice.exception.ResourceNotFoundException;
import com.cognizant.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    // Explicit constructor injection
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating a new user with email: {}", userDto.getEmail());
        User user = mapToEntity(userDto);
        User savedUser = userRepository.save(user);
        log.debug("Successfully saved user in database with ID: {}", savedUser.getId());
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Failed to update user. User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setPhone(userDto.getPhone());

        User updatedUser = userRepository.save(existingUser);
        log.debug("Successfully updated user in database with ID: {}", updatedUser.getId());
        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.error("Failed to delete user. User not found with ID: {}", id);
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        log.debug("Successfully deleted user with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });
        log.debug("User found with ID: {}", id);
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users in database", users.size());
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert UserDto to User entity without builder
    private User mapToEntity(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return user;
    }

    // Helper method to convert User entity to UserDto without builder
    private UserDto mapToDto(User entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        return dto;
    }
}
