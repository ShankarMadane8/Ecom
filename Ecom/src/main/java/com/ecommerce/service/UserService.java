package com.ecommerce.service;

import java.util.List;

import com.ecommerce.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.entity.User;

public interface UserService {
    ResponseEntity<Object> createUser(UserDto userDto);
    User updateUser(int id, UserDto userDto);
    void deleteUser(int id);
    User getUserById(int id);
    List<User> getAllUsers();
}
