package newREALs.backend.service;

// JWT 방식으로 토큰 생성 & 검증

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {
    private SecretKey key;

    @Value("${jwt.expiration-time}")
    private long expireTime; // 예: 3600000 (1시간)

    @PostConstruct
    public void init() {
        // .env 파일 로드
        Dotenv dotenv = Dotenv.load();
        String secretKey = dotenv.get("JWT_SECRET_KEY"); // .env 파일에서 JWT_SECRET_KEY 가져옴
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String name, String email) {
        Claims claims = Jwts.claims().setSubject(email); // email을 subject로 설정
        claims.put("name", name);

        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key)
                    .build().parseClaimsJws(token);

            return claims .getBody() .getExpiration()
                    .after(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getName(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build().parseClaimsJws(token).getBody();
        return claims.get("name", String.class);
    }
}
