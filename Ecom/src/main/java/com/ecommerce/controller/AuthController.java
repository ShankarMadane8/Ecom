package com.ecommerce.controller;

import com.ecommerce.config.JwtUtil;
import com.ecommerce.config.UserDetailServiceImp;
import com.ecommerce.dao.UserRepository;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.request.JwtRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailServiceImp userDetailServiceImp;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;


    private static final String GITHUB_CLIENT_ID = "Ov23limPvurAUkypiyUp";
    private static final String GITHUB_CLIENT_SECRET = "816c3934f0aa446a7fc0a85956ec6fe9dc662f94";

    private static final String FACEBOOK_CLIENT_ID = "1177202586887125";
    private static final String FACEBOOK_CLIENT_SECRET = "155ca194ce0e2d97064d2e7c1dadaff0";



    //    @Hidden
    @Operation(summary = "Authenticate a user", description = "Authenticate user and generate a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated and token generated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public String login(@RequestBody JwtRequest jwtRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(jwtRequest.getEmail(), jwtRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException e) {
            System.err.println("UserNotFoundException in AuthenticationController");
            e.printStackTrace();
            throw new UsernameNotFoundException("User Not Found Exception");
        }

        UserDetails userDetails = this.userDetailServiceImp.loadUserByUsername(jwtRequest.getEmail());
        return jwtUtil.generateToken(userDetails);
    }

    @PostMapping("/github/callback")
    public ResponseEntity<?> handleGitHubCallback(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        System.err.print("code: " + code);
        System.out.print("=====================");

        String accessToken = getAccessTokenFromGitHub(code);
        Map<String, Object> userDetails = getUserDetailsFromGitHub(accessToken);
        System.err.print("userDetails: " + userDetails);

        String email = userDetails.get("email") != null ? (String) userDetails.get("email") : (String) userDetails.get("login");

        String fullName = (String) userDetails.get("name");
        String firstName = "";
        String lastName = "";

        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }
        String password = firstName + "8" + lastName;
        if (!userRepository.existsByEmail(email)) {
            UserDto adminDto = new UserDto();
            adminDto.setEmail(email);
            adminDto.setFirstName(firstName);
            adminDto.setLastName(lastName);
            adminDto.setPhoneNo("1234567890");
            adminDto.setRole("ADMIN");
            adminDto.setUserType("Admin");
            adminDto.setPassword(passwordEncoder.encode(password));

            User adminUser = new User(adminDto);
            userRepository.save(adminUser);
        }

        return ResponseEntity.ok(generateToken(email, password));
    }

    @PostMapping("/facebook/callback")
    public ResponseEntity<?> handleFacebookCallback(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        System.err.print("facebook code: " + code);
        System.out.print("=====================");

        String accessToken = getAccessTokenFromFacebook(code);
        Map<String, Object> userDetails = getUserDetailsFromFacebook(accessToken);
        System.err.print("userDetails: " + userDetails);

        String email = (String) userDetails.get("email");
        String fullName = (String) userDetails.get("name");
        String firstName = "";
        String lastName = "";

        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }
        String password = firstName + "8" + lastName;
        if (!userRepository.existsByEmail(email)) {
            UserDto adminDto = new UserDto();
            adminDto.setEmail(email);
            adminDto.setFirstName(firstName);
            adminDto.setLastName(lastName);
            adminDto.setPhoneNo("1234567890");
            adminDto.setRole("ADMIN");
            adminDto.setUserType("Admin");
            adminDto.setPassword(passwordEncoder.encode(password));

            User adminUser = new User(adminDto);
            userRepository.save(adminUser);
        }

        return ResponseEntity.ok(generateToken(email, password));
    }

    public String generateToken(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException e) {
            System.err.println("UserNotFoundException in AuthenticationController");
            e.printStackTrace();
            throw new UsernameNotFoundException("User Not Found Exception");
        }

        UserDetails userDetails = this.userDetailServiceImp.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails);
    }

    private String getAccessTokenFromGitHub(String code) {


        RestTemplate restTemplate = new RestTemplate();
        String url = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> requestBody = Map.of(
                "client_id", GITHUB_CLIENT_ID,
                "client_secret", GITHUB_CLIENT_SECRET,
                "code", code
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, String> responseBody = response.getBody();
        assert responseBody != null;
        return responseBody.get("access_token");
    }

    private Map<String, Object> getUserDetailsFromGitHub(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> userProfile = response.getBody();
        Map<String, Object> user = new HashMap<>();
        user.put("login", userProfile.get("login"));
        user.put("id", userProfile.get("id"));
        user.put("name", userProfile.get("name"));
        user.put("email", userProfile.getOrDefault("email", "not found"));
        return user;
    }

    private String getAccessTokenFromFacebook(String code) {

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/v10.0/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> requestBody = Map.of(
                "client_id", FACEBOOK_CLIENT_ID,
                "client_secret", FACEBOOK_CLIENT_SECRET,
                "code", code,
                "redirect_uri", "http://localhost:3000/login?provider=facebook"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, String> responseBody = response.getBody();
        assert responseBody != null;
        return responseBody.get("access_token");
    }

    private Map<String, Object> getUserDetailsFromFacebook(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/me?fields=name,email";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> userProfile = response.getBody();
        System.err.println("userProfile : "+userProfile);
        Map<String, Object> user = new HashMap<>();
        user.put("name", userProfile.get("name"));
        user.put("email", userProfile.getOrDefault("email", "not found"));
        return user;
    }
}
