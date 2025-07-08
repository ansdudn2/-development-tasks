package com.example.developmenttasks.auth.service;

import com.example.developmenttasks.auth.dto.request.LoginRequest;
import com.example.developmenttasks.auth.dto.request.SignupRequest;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "USERNAME_EXISTS","이미 존재하는 사용자입니다.");
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
}
