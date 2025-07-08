package com.example.developmenttasks.auth.dto.request;

import lombok.Getter;

@Getter
public class SignupRequest {
    private String username;
    private String password;
    private String nickname;
}
