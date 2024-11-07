package newREALs.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import newREALs.backend.service.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, jakarta.servlet.ServletException{

        String token = resolveToken(request);

        // 헤더에 작성된 토큰 있는지 & 유효한지
        if (token != null && tokenService.validateToken(token)) {
            // 토큰에서 secret key 사용해서 이메일 가져옴
            String email = tokenService.getEmail(token);

            log.debug("JwtAuthFilter - Valid token found. Email: {}", email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 인증된 회원 정보를 SecurityContextHolder에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        else{
            log.debug("JwtAuthFilter - Invalid token");
        }

        // 토큰 없거나 만료된 토큰 -> 다시 소셜 로그인 진행
        filterChain.doFilter(request, response);

    }

    // Request Header 에서 토큰 정보를 꺼내옴
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    // 로그 추가: 쿠키에서 토큰 추출
                    log.debug("JwtAuthFilter - Token extracted from cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        log.debug("JwtAuthFilter - No access token found in cookies.");
        return null;
    }
}
