package com.ecommerce.auth.model;

public enum RoleTypeEnum {

    ROLE_USER("Regular customer"),
    ROLE_SELLER("Seller who manages inventory"),
    ROLE_ADMIN("System administrator");

    private String description;

    RoleTypeEnum(String description) {
        this.description = description;
    }
//    @Override
//    public String toString(){
//        return name();
//    }
}
