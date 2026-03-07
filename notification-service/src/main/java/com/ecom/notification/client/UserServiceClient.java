package com.ecom.notification.client;

import com.ecom.notification.dto.UserDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name="AUTH-SERVICE",
        path = "/api/v1/user"    )
public interface UserServiceClient {

    @GetMapping("/{id}/details")
    UserDetailDTO getDetails(@PathVariable UUID id);

}
