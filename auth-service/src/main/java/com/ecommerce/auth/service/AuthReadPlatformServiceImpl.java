package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.UserDetailDTO;
import com.ecommerce.auth.exception.UserNotFoundException;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthReadPlatformServiceImpl implements AuthReadPlatformService{
    private final UserRepository userRepository;

    public AuthReadPlatformServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailDTO getUserDetails(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(()->new UserNotFoundException("User with id= " + id + "not found"));
        String fullName = user.getFirstName()+" "+user.getLastName();
        return new UserDetailDTO(fullName, user.getEmail(),user.getMobileNo());
    }
}
