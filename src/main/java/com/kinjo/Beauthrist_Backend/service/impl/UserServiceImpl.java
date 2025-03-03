package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.LoginRequest;
import com.kinjo.Beauthrist_Backend.dto.ProductDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.UserDto;
import com.kinjo.Beauthrist_Backend.entity.PasswordResetToken;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.enums.UserRole;
import com.kinjo.Beauthrist_Backend.exception.InvalidCredentialException;
import com.kinjo.Beauthrist_Backend.exception.InvalidPasswordException;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.exception.UserAlreadyExistsException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.PasswordResetTokenRepo;
import com.kinjo.Beauthrist_Backend.repository.UserRepo;
import com.kinjo.Beauthrist_Backend.security.JwtUtils;
import com.kinjo.Beauthrist_Backend.service.EmailService;
import com.kinjo.Beauthrist_Backend.service.interf.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EntityDtoMapper entityDtoMapper;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final EmailService emailService;


    @Override
    public Response getUserInfoAndOrdersHistory() {
        User user = getLoginUser();

        User userWithDetails = userRepo.findByIdWithAddressAndOrders(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Map the user entity to UserDto with address and order history
        UserDto userDto = entityDtoMapper.mapUserToDtoPlusAddressAndOrderHistory(userWithDetails);

        // Return the response
        return Response.builder()
                .status(200)
                .user(userDto)
                .build();
    }

    public List<UserDto> getAllUsersWithAddressAndOrders() {
        List<User> users = userRepo.findAllWithAddressAndOrders();
        return users.stream()
                .map(entityDtoMapper::mapUserToDtoPlusAddressAndOrderHistory)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserByIdWithAddress(Long id) {
        // Fetch user with address from the repository
        User user = userRepo.findByIdWithAddress(id).orElse(null);
        if (user == null) {
            return null; // Handle user not found
        }

        // Map the user entity to UserDto
        return entityDtoMapper.mapUserToDtoBasic(user);
    }

    @Override
    public List<UserDto> getAllUsersWithAddress() {
        // Fetch all users with addresses from the repository
        List<User> users = userRepo.findAllWithAddress();

        // Map each user entity to UserDto
        return users.stream()
                .map(entityDtoMapper::mapUserToDtoBasic)
                .collect(Collectors.toList());
    }


    private boolean isPasswordStrong(String password) {
        String strongPasswordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$";
        return Pattern.compile(strongPasswordRegex).matcher(password).matches();
    }


    @Override
    public Response registerUser(UserDto registrationRequest) {
        // Check if the email is already registered
        if (userRepo.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered");
        }
        if (userRepo.findByPhoneNumber(registrationRequest.getPhoneNumber()).isPresent()) {
            throw new UserAlreadyExistsException("User with this phone number already exists");
        }

        // Validate password strength
        if (!isPasswordStrong(registrationRequest.getPassword())) {
            throw new InvalidPasswordException("Password must be at least 8 characters long.");
        }

        // Proceed with registration
        UserRole role = UserRole.ROLE_USER;
        if (registrationRequest.getRole() != null) {
            if (registrationRequest.getRole().equalsIgnoreCase("role_admin")) {
                role = UserRole.ROLE_ADMIN;
            } else if (registrationRequest.getRole().equalsIgnoreCase("role_customer_support")) {
                role = UserRole.ROLE_CUSTOMER_SUPPORT;
            }else if (registrationRequest.getRole().equalsIgnoreCase("role_company")) {
                role = UserRole.ROLE_COMPANY;
            }
        }

        // Create the User entity
        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .companyName(registrationRequest.getCompanyName()) // Set company-specific details
                .businessRegistrationNumber(registrationRequest.getBusinessRegistrationNumber())
                .userRole(role)
                .build();

        // Save the User entity to the database
        User savedUser = userRepo.save(user);

        emailService.sendEmail(
                savedUser.getEmail(),
                "Welcome to KinjoMarket!",
                "Dear " + savedUser.getName() + ",\n\nThank you for registering with KinjoMarket!"
        );

        // Debugging: Print the saved user's ID
        System.out.println("Saved User ID: " + savedUser.getId());

        // Map the saved User entity to UserDto
        UserDto userDto = entityDtoMapper.mapUserToDtoBasic(savedUser);

        // Debugging: Print the UserDto's ID
        System.out.println("UserDto ID: " + userDto.getId());

        // Build and return the response
        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("User Successfully Added")
                .user(userDto) // Include the UserDto in the response
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        // Find the user by email
        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));

        // Check if the password matches
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Password does not match");
        }

        // Generate a token (if using JWT)
        String token = jwtUtils.generateToken(user);
        UserDto userDto = entityDtoMapper.mapUserToDtoBasic(user);  // added for local storage


        return Response.builder()
                .status(200)
                .token(token)
                .expirationTime("2 days")
                .role(user.getUserRole().name())
                .user(userDto)   // added for local storage
                .build();
    }

    @Override
    public Response getAllUser() {
        List<User> users = userRepo.findAll();
        List<UserDto> userDtos = users.stream()
                .map(entityDtoMapper::mapUserToDtoBasic)
                .toList();

        return Response.builder()
                .status(200)
                .message("Successful")
                .userList(userDtos)
                .build();
    }

    @Override
    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    @Override
    public Response getUserInfoAndOrderHistory() {
        User user = getLoginUser();
        UserDto userDto = entityDtoMapper.mapUserToDtoPlusAddressAndOrderHistory(user);
        log.info("UserDto: {}", userDto);

        return Response.builder()
                .status(200)
                .user(userDto)
                .build();
    }


    @Override
    public Response requestPasswordReset(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if a token already exists for the user
        Optional<PasswordResetToken> existingToken = passwordResetTokenRepo.findByUser(user);

        PasswordResetToken resetToken;
        if (existingToken.isPresent()) {
            // Update the existing token
            resetToken = existingToken.get();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // Reset expiry date
        } else {
            // Create a new token
            resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        }

        // Save the token
        passwordResetTokenRepo.save(resetToken);

        // Send the reset link to the user's email
        String resetLink = "https://www.kinjomarket.com/reset-password?token=" + resetToken.getToken();
        emailService.sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "To reset your password, click the link below:\n" + resetLink
        );

        return Response.builder()
                .status(200)
                .message("Password reset link sent to your email")
                .build();
    }

    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepo.deleteByExpiryDateBefore(now);
        System.out.println("Expired tokens deleted at: " + now);
    }

    @Override
    public Response resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid token"));

        // Check if the token has expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialException("Token has expired");
        }

        // Update the user's password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // Delete the token after use
        passwordResetTokenRepo.delete(resetToken);

        return Response.builder()
                .status(200)
                .message("Password reset successfully")
                .build();
    }


    @Override
    public Response updateUserProfile(UserDto userDto) {
        // Get the currently logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Fetch the user from the database
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update the user's profile information
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userDto.getPhoneNumber());
        }
        // Save the updated user to the database
        User updatedUser = userRepo.save(user);

        // Map the updated user to a DTO
        UserDto updatedUserDto = entityDtoMapper.mapUserToDtoBasic(updatedUser);

        // Return the response
        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("Profile updated successfully")
                .user(updatedUserDto)
                .build();
    }

    @Override
    public Response getAllUsersWithRoleCompany() {
        // Fetch all users with ROLE_COMPANY from the repository
        List<User> users = userRepo.findAllByRoleCompany();

        // Map the entities to DTOs
        List<UserDto> userDtos = users.stream()
                .map(entityDtoMapper::mapUserToDtoBasic)
                .collect(Collectors.toList());

        // Return the response
        return Response.builder()
                .status(200)
                .message("Successfully fetched all users with ROLE_COMPANY")
                .userList(userDtos)
                .build();
    }

    @Override
    public Response getCompanyWithProducts(Long companyId) {
        User company = userRepo.findCompanyWithProductsById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        // Map the company and its products to DTOs
        UserDto companyDto = entityDtoMapper.mapUserToDtoBasic(company);
        List<ProductDto> productDtos = company.getProducts().stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Successfully fetched company with products")
                .user(companyDto)
                .productList(productDtos)
                .build();
    }

    @Override
    public Response loginCompany(LoginRequest loginRequest) {
        // Find the user by email
        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));

        // Check if the password matches
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Password does not match");
        }

        // Ensure the user has the ROLE_COMPANY role
        if (!user.getUserRole().equals(UserRole.ROLE_COMPANY)) {
            throw new InvalidCredentialException("This account is not authorized as a company");
        }

        // Generate a token (if using JWT)
        String token = jwtUtils.generateToken(user);

        // Map the user entity to a DTO
        UserDto userDto = entityDtoMapper.mapUserToDtoBasic(user);

        // Return the response
        return Response.builder()
                .status(200)
                .token(token)
                .expirationTime("2 days") // Adjust based on your token expiration logic
                .role(user.getUserRole().name())
                .user(userDto) // Include the UserDto in the response
                .build();
    }
}
