package com.ecommerce.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleTypeEnum roleTypeEnum;

    @Column(nullable = false)
    private String description;

    protected Role() {
    }

    public Role(RoleTypeEnum roleTypeEnum, String description) {
        this.roleTypeEnum = roleTypeEnum;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public RoleTypeEnum getRoleTypeEnum() {
        return roleTypeEnum;
    }

    public void setRoleTypeEnum(RoleTypeEnum roleTypeEnum) {
        this.roleTypeEnum = roleTypeEnum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
