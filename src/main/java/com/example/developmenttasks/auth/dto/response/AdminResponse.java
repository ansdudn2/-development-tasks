package com.example.developmenttasks.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminResponse {
    private String username;
    private String nickname;
    private List<RoleDto> roles;

    @Getter
    @AllArgsConstructor
    public static class RoleDto {
        private String role;
    }
}
