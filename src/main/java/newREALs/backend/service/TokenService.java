package newREALs.backend.service;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.domain.Accounts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

// jwt 토큰 생성
@Slf4j
@Service
public class TokenService {
    private Key key;
    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.load();
        // .env 파일에서 JWT_SECRET_KEY 가져옴
        String secretKey = dotenv.get("JWT_SECRET_KEY");
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public String generateToken(Accounts account) {
        Date date = new Date();

        // claim -> body 영역이라고 생각하면 됨...
        return Jwts.builder()
                .setSubject(account.getEmail())
                .claim("name", account.getName())
                .claim("profilePath", account.getProfilePath())
                .setExpiration(new Date(date.getTime() + expirationTime))
                .setIssuedAt(date)
                .signWith(key, SignatureAlgorithm.HS512) // 암호화 알고리즘
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("잘못된 JWT 토큰이에요: {}", e.getMessage());
            return false;
        }
    }

    public String getEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
