package com.example.developmenttasks.common.security;

import com.example.developmenttasks.auth.entity.UserRole;
import com.example.developmenttasks.common.dto.AuthMember;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long TOKEN_VALID_TIME = 1000L*60*60*1; //1시간

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(Long userId, String username, List<UserRole> roles) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("username", username);
        claims.put("roles", roles.stream().map(Enum::name).collect(Collectors.toList()));

        Date now = new Date();
        Date validity = new Date(now.getTime() + TOKEN_VALID_TIME);

        return jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long userId = Long.valueOf(claims.getSubject());
        String username = (String)claims.get("username");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>)claims.get("roles");

        AuthMember authMember = new AuthMember(
                userId,
                username,
                roles.stream().map(UserRole::valueOf).collect(Collectors.toList())
        );
        return new UsernamePasswordAuthenticationToken(authMember,"",authMember.getAuthorities);
    }
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (jwtException | IllegalArgumentException e){
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.builder()
                .setSigningKey(key)
                .buuld().
                parseClaimsJws(token).
                getBody();
    }
}
