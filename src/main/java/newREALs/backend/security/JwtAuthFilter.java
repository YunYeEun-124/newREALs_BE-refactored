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
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {

        String token = tokenService.extractTokenFromHeader(request);

        if (token != null && tokenService.validateToken(token)) {
            // 토큰 타입 확인
            String tokenType = tokenService.getTokenType(token);
            Long userId = tokenService.extractUserIdFromToken(token);

            if ("temporary".equals(tokenType)) {
                log.debug("JwtAuthFilter - 임시 토큰으로 요청. UserId: {}", userId);
                // 임시 토큰은 특정 API에만 접근 가능하도록 제한
                if (!request.getRequestURI().startsWith("/accounts/register")) {
                    log.warn("JwtAuthFilter - 임시 토큰으로 접근이 차단된 API 요청: {}", request.getRequestURI());
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("text/plain; charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                    response.getWriter().write("임시 토큰으로는 접근할 수 없습니다.");
                    return;
                }
            } else if ("access".equals(tokenType)) {
                log.debug("JwtAuthFilter - 정식 토큰으로 요청. UserId: {}", userId);
                // 정식 토큰은 모든 보호된 API에 접근 허용
            } else {
                log.warn("JwtAuthFilter - 알 수 없는 토큰 타입: {}", tokenType);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/plain; charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                response.getWriter().write("잘못된 토큰입니다.");
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.debug("JwtAuthFilter - 유효하지 않은 토큰");
        }

        filterChain.doFilter(request, response);
    }



}
