package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.LoginRequest;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.UserDto;
import com.kinjo.Beauthrist_Backend.service.interf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody UserDto registrationRequest) {
        Response response = userService.registerUser(registrationRequest);
        if (response.getStatus() == 400) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-company")
    public ResponseEntity<?> registerCompany(@RequestBody UserDto registrationRequest) {
        registrationRequest.setRole("ROLE_COMPANY");
        Response response = userService.registerUser(registrationRequest);
        if (response.getStatus() == 400) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> loginUser (@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Response> requestPasswordReset(@RequestParam String email) {
        return ResponseEntity.ok(userService.requestPasswordReset(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        return ResponseEntity.ok(userService.resetPassword(token, newPassword));
    }

}





