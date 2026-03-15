package com.ecommerce.auth.repository;

import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.RoleTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long > {
    Role findByRoleTypeEnum(RoleTypeEnum roleTypeEnum);
}
