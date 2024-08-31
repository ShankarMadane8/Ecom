package com.ecommerce.serviceImpl;


import java.util.List;
import java.util.Optional;

import com.ecommerce.constant.MessageConstant;
import com.ecommerce.dto.UserDto;
import com.ecommerce.exceptions.OrderNotFoundException;
import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.exceptions.UserNotFoundException;
import com.ecommerce.response.ResponseHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.dao.UserRepository;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @PostConstruct
    public void init() {
        createDefaultAdminUser();
    }

    public void createDefaultAdminUser() {
        String defaultEmail = "admin@gmail.com";
        if (!userRepository.existsByEmail(defaultEmail)) {
            UserDto adminDto = new UserDto();
            adminDto.setEmail(defaultEmail);
            adminDto.setFirstName("Admin");
            adminDto.setLastName("User");
            adminDto.setPhoneNo("1234567890");
            adminDto.setRole("ADMIN");
            adminDto.setUserType("Admin");
            adminDto.setPassword(passwordEncoder.encode("admin123"));

            User adminUser = new User(adminDto);
            userRepository.save(adminUser);
        }
    }



    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<Object> createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseHandler.generateResponse( 999,"User with email " + userDto.getEmail() + " already exists.", null);
        }
        User user = new User(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
       if(user.getAddresses()!=null){
           for (Address address : user.getAddresses()) {
               address.setUser(user);
           }
       }
        return ResponseHandler.generateResponse(HttpStatus.OK, MessageConstant.MSG_SUCCESS, userRepository.save(user));


    }

    @Override
    public User updateUser(int id, UserDto userDto) {
        User user = getUserById(id);
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNo(Long.parseLong(userDto.getPhoneNo()));
        user.setRole(userDto.getRole());
        user.setUserType(userDto.getUserType());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
