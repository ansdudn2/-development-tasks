package com.example.developmenttasks.auth;

import com.example.developmenttasks.auth.dto.request.LoginRequest;
import com.example.developmenttasks.auth.dto.request.SignupRequest;
import com.example.developmenttasks.auth.dto.response.SignupResponse;
import com.example.developmenttasks.auth.entity.User;
import com.example.developmenttasks.auth.entity.UserRole;
import com.example.developmenttasks.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    // 회원가입 성공
    @Test
    void signup_success() throws Exception {

        // dto 생성
        SignupRequest req = new SignupRequest("JIN HO", "12341234", "Mentos");

        // 요청 빌더에 contentType, content 포함
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("JIN HO"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

    }

    //회원가입 중복
    @Test
    void signup_duplicate() throws Exception {
        SignupRequest req = new SignupRequest("JIN HO", "12341234", "Mentos");

        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USERNAME_EXISTS"));
   }

   // 로그인 성공
   @Test
    void login_success() throws Exception {

        // 사전 회원가입
       mockMvc.perform(
               post("/signup")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(
                               new SignupRequest("JIN HO", "12341234", "Mentos")))
       );

       // 로그인 후 토큰 반환 확인
       mockMvc.perform(post("/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(
                               new LoginRequest("JIN HO", "12341234")
                       )))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").isNotEmpty());
   }

   // 로그인 실패
    @Test
    void login_fail() throws Exception {
        mockMvc.perform(
                post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("WRONG", "EEEE"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    // 일반 사용자로 관리자 api 호출
    @Test
    void grantAdmin_fail() throws Exception {

        // user 회원가입 & 로그인 -> 토큰 추출
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new SignupRequest("JIN HO", "12341234", "Mentos"))));
        String loginJson = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("JIN HO", "12341234"))))
                .andReturn().getResponse().getContentAsString();
        String userToken = objectMapper.readTree(loginJson).get("token").asText();

        // 관리자 api 호출 -> 접근 거부
        mockMvc.perform(patch("/admin/users/{id}/roles", 1)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    // admin 권한 부여 성공
    @Test
    void grantAdmin_success() throws Exception {

        // 일반 사용자 생성
        User user = new User("user", passwordEncoder.encode("pw"), "Normal");
        user .getRoles().add(UserRole.USER);
        user = userRepository.save(user);

        // admin 로그인
        User admin = new User("ADMIN", passwordEncoder.encode("adminpw"), "Boss");
        admin .getRoles().addAll(Set.of(UserRole.USER, UserRole.ADMIN));
        userRepository.save(admin);
        String loginJson = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("ADMIN", "adminpw"))))
                .andReturn().getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(loginJson).get("token").asText();

        // 관리자 api 호출
        mockMvc.perform(patch("/admin/users/{id}/roles", user.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    // 존재하지 않는 사용자 권한 부여 시도
    @Test
    void grantAdmin_notfound() throws Exception {

        // admin 토큰 준비
        User admin = new User("ADMIN", passwordEncoder.encode("adminpw"), "Boss");
        admin.getRoles().addAll(Set.of(UserRole.USER, UserRole.ADMIN));
        userRepository.save(admin);
        String loginJson = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("ADMIN", "adminpw"))))
                .andReturn().getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(loginJson).get("token").asText();

        // 잘못된 userId 호출
        mockMvc.perform(patch("/admin/users/{id}/roles", 999)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }
}
