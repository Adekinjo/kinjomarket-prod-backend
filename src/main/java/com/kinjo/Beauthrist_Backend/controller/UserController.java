package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.UserDto;
import com.kinjo.Beauthrist_Backend.exception.InvalidCredentialException;
import com.kinjo.Beauthrist_Backend.service.interf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get-all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response>  getAllUsers(){
        return ResponseEntity.ok(userService.getAllUser());
    }
    @GetMapping("/my-info")
    public ResponseEntity<Response> getUserInfoAndOrderHistory() {
        return ResponseEntity.ok(userService.getUserInfoAndOrdersHistory());
    }

    // to update a user
    @PutMapping("/update")
    public ResponseEntity<Response> updateUserProfile(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUserProfile(userDto));
    }

    // to get a user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserByIdWithAddress(id);
        return userDto != null ? ResponseEntity.ok(userDto) : ResponseEntity.notFound().build();
    }


    // to get all users only
    @GetMapping("/get-alls")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUser() {
        List<UserDto> users = userService.getAllUsersWithAddress();
        return ResponseEntity.ok(users);
    }

    // to get all user information
    @GetMapping("/get-all-info")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsersInfo() {
        List<UserDto> users = userService.getAllUsersWithAddressAndOrders();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role-company")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Only admins can access this endpoint
    public ResponseEntity<Response> getAllUsersWithRoleCompany() {
        try {
            Response response = userService.getAllUsersWithRoleCompany();
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialException e) {
            return ResponseEntity.status(403).body(
                    Response.builder()
                            .status(403)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Response.builder()
                            .status(500)
                            .message("Failed to fetch users with ROLE_COMPANY: " + e.getMessage())
                            .build()
            );
        }
    }

}
