package com.ecommerce.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String username;

    @Email
    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false,unique = true)
    private String mobileNo;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    protected User() {
    }

    public User(Long id, String username, String email, String mobileNo, String firstName, String lastName, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.mobileNo = mobileNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    public User(String username, String email, String mobileNo, String firstName, String lastName, String password) {
        this.username = username;
        this.email = email;
        this.mobileNo = mobileNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
