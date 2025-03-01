package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.LoginRequest;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.UserDto;
import com.kinjo.Beauthrist_Backend.entity.User;

import java.util.List;

public interface UserService {
    Response registerUser(UserDto registrationRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUser();

    User getLoginUser();

    Response getUserInfoAndOrderHistory();

    Response requestPasswordReset(String email);

    Response resetPassword(String token, String newPassword);

    Response updateUserProfile(UserDto userDto);

    UserDto getUserByIdWithAddress(Long id); // Fetch user with address by ID

    List<UserDto> getAllUsersWithAddress();

    Response getUserInfoAndOrdersHistory();

    // Fetch all users with their addresses and orders
    List<UserDto> getAllUsersWithAddressAndOrders();

    Response getAllUsersWithRoleCompany();

    Response getCompanyWithProducts(Long companyId);
    Response loginCompany(LoginRequest loginRequest);


}
