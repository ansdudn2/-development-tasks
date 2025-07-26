package com.example.developmenttasks.common.dto;

import com.example.developmenttasks.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class AuthMember {
    private Long id;
    private String username;
    private List<UserRole> userRoles;

    public List<GrantedAuthority> getAuthorities() {
        return userRoles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toUnmodifiableList());
    }
}
