package com.example.auth_service.api;

import com.example.auth_service.config.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Person {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Role role;
}
