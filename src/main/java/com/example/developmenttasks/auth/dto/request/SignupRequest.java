package com.example.developmenttasks.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String nickname;
}
