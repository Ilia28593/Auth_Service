package com.example.auth_service.api;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class JwtRequest {
    @NonNull
    private String login;
    @NonNull
    private String password;

}
