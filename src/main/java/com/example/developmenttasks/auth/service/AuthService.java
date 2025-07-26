package com.example.developmenttasks.auth.service;

import com.example.developmenttasks.auth.dto.request.LoginRequest;
import com.example.developmenttasks.auth.dto.request.SignupRequest;
import com.example.developmenttasks.auth.dto.response.AdminResponse;
import com.example.developmenttasks.auth.entity.User;
import com.example.developmenttasks.auth.entity.UserRole;
import com.example.developmenttasks.auth.repository.UserRepository;
import com.example.developmenttasks.common.exception.CustomException;
import com.example.developmenttasks.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "USERNAME_EXISTS","이미 가입된 사용자입니다.");
        }

        User user = new  User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
                );
        user.addRole(UserRole.USER);
        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new CustomException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다."));

    if (!passwordEncoder.matches(request.getPassword(),user.getPassword())){
        throw new CustomException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다.");
    }
    return jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRoles());
    }

    public AdminResponse grantAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."));
        user.getRoles().remove(UserRole.USER);
        user.addRole(UserRole.ADMIN);
        User saved = userRepository.save(user);

        List<AdminResponse.RoleDto> dtos = saved.getRoles().stream()
                .map(r -> new AdminResponse.RoleDto(r.name()))
                .collect(Collectors.toList());

        return new AdminResponse(saved.getUsername(), saved.getNickname(), dtos);
    }
}
