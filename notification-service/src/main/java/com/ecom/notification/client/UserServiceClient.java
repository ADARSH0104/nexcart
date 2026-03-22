package com.ecom.notification.client;

import com.ecom.notification.dto.UserDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="AUTH-SERVICE",
        path = "/api/v1/auth"    )
public interface UserServiceClient {

    @GetMapping("/{id}/details")
    UserDetailDTO getDetails(@PathVariable Long id);

}
