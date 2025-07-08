package com.example.developmenttasks.common.dto;

import com.example.developmenttasks.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthMember {
    private Long id;
    private String username;
    private List<UserRole> userRoles;
}
