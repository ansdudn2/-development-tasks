package com.example.developmenttasks.auth.controller;

import com.example.developmenttasks.auth.service.AuthService;
import com.example.developmenttasks.common.security.annotation.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminController {

    private final AuthService authService;

    @PatchMapping("/{userId}/roles")
    @Admin
    public ResponseEntity<Void> grantAdmin(@PathVariable Long userId) {
        authService.grantAdminRole(userId);
        return ResponseEntity.ok().build();
    }
}
