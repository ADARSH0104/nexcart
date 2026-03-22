package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.UserDetailDTO;

public interface AuthReadPlatformService {

    UserDetailDTO getUserDetails(Long id);
}
