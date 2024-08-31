package com.ecommerce.controller;

import java.util.List;
import java.util.Objects;

import com.ecommerce.constant.MessageConstant;
import com.ecommerce.dto.UserDto;
import com.ecommerce.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.entity.User;
import com.ecommerce.serviceImpl.UserServiceImpl;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@CrossOrigin
@Tag(name = "User", description = "Operations related to user management")
public class UserController {
	
	@Autowired
	private  UserServiceImpl userService;

    @Operation(summary = "Register a new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        userDto.setRole("USER");
        return userService.createUser(userDto);
    }

    @Operation(summary = "Update user details", description = "Update an existing user identified by the user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID or input"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("update/{id}")
    public ResponseEntity<Object> updateUser(@Valid @PathVariable int id, @RequestBody UserDto userDto) {
        User user = userService.updateUser(id, userDto);
        return ResponseHandler.generateResponse(HttpStatus.OK, MessageConstant.MSG_SUCCESS, user);
    }

    @Operation(summary = "Delete a user", description = "Delete an existing user by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
