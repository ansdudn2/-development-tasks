package com.example.developmenttasks.auth.service;

import com.example.developmenttasks.auth.dto.RoleDto;
import com.example.developmenttasks.auth.dto.request.LoginRequest;
import com.example.developmenttasks.auth.dto.request.SignupRequest;
import com.example.developmenttasks.auth.dto.response.AdminResponse;
import com.example.developmenttasks.auth.dto.response.SignupResponse;
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

    // 회원가입
    public SignupResponse signup(SignupRequest request) {
        // 중복 가입 체크
        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "USERNAME_EXISTS","이미 가입된 사용자입니다.");
        }

        // 유저 생성 및 기본 role
        User user = new  User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
                );
        user.addRole(UserRole.USER);

        User saved = userRepository.save(user);

        // dto용 roles 리스트로 맵핑
        List<String> roles = saved.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toList());

        // 응답 dto 생성 및 반환
        return new SignupResponse(saved.getUsername(), saved.getNickname(), roles);
    }

    // 로그인
    public String login(LoginRequest request) {
        // 사용자 조회 및 존재 여부 검증
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new CustomException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다."));
    // 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(),user.getPassword())){
        throw new CustomException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다.");
    }
    // jwt 토큰 발급
    return jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRoles());
    }

    // 관리자 권한 부여
    public AdminResponse grantAdminRole(Long userId) {
        // 대상 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."));
        if (!user.getRoles().contains(UserRole.ADMIN)){
            //기존 user 역할 제거 및 admin 역할 추가
            user.getRoles().remove(UserRole.USER);
            user.addRole(UserRole.ADMIN);

            User saved = userRepository.save(user);

        }

        // dto용 roles 리스트로 맵핑
        List<String> roles = user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toList());

        // 응답 dto 생성 및 반환
        return new AdminResponse(user.getUsername(), user.getNickname(), roles);
    }
}
