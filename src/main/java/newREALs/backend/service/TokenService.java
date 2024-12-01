package newREALs.backend.service;


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
    @Value("${jwt.secret-key}")
    private String secretKey;
    // JWT_SECRET_KEY는 .env 파일에서 가져오기
    @PostConstruct
    public void init() {

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Value("${jwt.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    public String generateAccessToken(Accounts account) {
        Date date = new Date();

        // claim -> body 영역이라고 생각하면 됨...
        return Jwts.builder()
                .setSubject(String.valueOf(account.getId()))
                .claim("email", account.getEmail())
                .claim("name", account.getName())
                .claim("profilePath", account.getProfilePath())
                .claim("type", "access")
                .setExpiration(new Date(date.getTime() + accessExpirationTime))
                .setIssuedAt(date)
                .signWith(key, SignatureAlgorithm.HS512) // 암호화 알고리즘
                .compact();
    }

    public String generateRefreshToken(Accounts account) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(account.getId()))
                .setExpiration(new Date(now.getTime() + refreshExpirationTime))
                .setIssuedAt(now)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


   public boolean validateToken(String token) {
       try {
           Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
           log.debug("TokenService - 토큰 유효함: {}", token);
           return true;
       } catch (Exception e) {
           log.error("잘못된 JWT 토큰이에요: {}, 예외: {}", token, e.getMessage());
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
        String token = extractTokenFromHeader(request);

        if (token == null || !validateToken(token)) {
            throw new SecurityException("유효하지 않은 토큰입니다.");
        }

        return extractUserIdFromToken(token);
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


    public String generateTemporaryToken(String email,String name, String profilePath){
        String temporaryToken = Jwts.builder()
                .setSubject(email) // 이메일을 Subject로 사용
                .claim("name", name)
                .claim("profilePath", profilePath)
                .claim("type", "temporary") // 임시 토큰 타입
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000)) // 30분 유효
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS512)
                .compact();
        return temporaryToken;
    }

    public String getTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("type", String.class); // 클레임에서 'type' 추출
    }

    public Claims validateAndParseToken(String token, String expectedType) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tokenType = claims.get("type", String.class);
        if (!expectedType.equals(tokenType)) {
            throw new IllegalArgumentException("유효하지 않은 토큰 타입입니다.");
        }

        return claims;
    }

    public String extractEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // 이메일 반환
    }




}
