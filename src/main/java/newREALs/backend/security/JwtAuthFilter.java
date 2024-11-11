package newREALs.backend.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import newREALs.backend.service.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    @Override
    public void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {

        // 헤더에서 토큰을 추출 (tokenService에 정의되어있음)
        String token = tokenService.extractTokenFromHeader(request);

        if (token != null && tokenService.validateToken(token)) {
            // 유효한 토큰 -> userId를 추출하고 인증 정보 설정
            Long userId = tokenService.extractUserIdFromToken(token);
            log.debug("JwtAuthFilter - 헤더에서 추출한 토큰의 user Id: {}", userId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.debug("JwtAuthFilter - 토큰 정보 잘못됨");
        }

        filterChain.doFilter(request, response);
    }
}
