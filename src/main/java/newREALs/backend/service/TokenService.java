package newREALs.backend.service;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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

    // JWT_SECRET_KEY는 .env 파일에서 가져오기
    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.load();
        String secretKey = dotenv.get("JWT_SECRET_KEY");
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public String generateToken(Accounts account) {
        Date date = new Date();

        // claim -> body 영역이라고 생각하면 됨...
        return Jwts.builder()
                .setSubject(String.valueOf(account.getId()))
                .claim("email", account.getEmail())
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

    // 헤더에서 토큰 추출
    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            log.debug("TokenService - 헤더에서 토큰 찾음");
            return bearerToken.substring(7); // "Bearer " 이후의 토큰 부분 추출
        }
        log.debug("TokenService -  헤더에 토큰 없음.");
        return null;
    }

    public Long getUserId(HttpServletRequest request){
        return extractUserIdFromToken( extractTokenFromHeader(request));

    }
    // 토큰으로 userId 추출
    public Long extractUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Long userId = Long.parseLong(claims.getSubject());
        log.debug("TokenService - 헤더에서 추출한 토큰의 user Id: {}", userId);
        return userId;
    }
}
