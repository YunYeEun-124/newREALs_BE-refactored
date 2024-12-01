package newREALs.backend.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        // 특정 경로 제외 (예: 리프레시 토큰 재발급)
        if ("/auth/refresh".equals(requestURI)) {
            log.debug("JwtAuthFilter - 토큰 검증을 건너뛴 경로 요청: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        String token = tokenService.extractTokenFromHeader(request);

        try {
            if (token == null || token.isEmpty()) {
                log.warn("JwtAuthFilter - 토큰이 없습니다.");
                sendUnauthorizedResponse(response, "Bearer 토큰이 필요합니다.");
                return; // 요청 중단
            }

            if (!tokenService.validateToken(token)) {
                log.warn("JwtAuthFilter - 유효하지 않은 토큰입니다.");
                sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
                return; // 요청 중단
            }

            String tokenType = tokenService.getTokenType(token);
            if ("temporary".equals(tokenType)) {
                log.debug("JwtAuthFilter - 임시 토큰으로 요청");
                String email = tokenService.extractEmailFromToken(token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // `/accounts/register` 외의 API는 접근 차단
                if (!request.getRequestURI().startsWith("/accounts/register")) {
                    log.warn("JwtAuthFilter - 임시 토큰으로 접근이 차단된 API 요청: {}", request.getRequestURI());
                    sendForbiddenResponse(response, "임시 토큰으로는 접근할 수 없습니다.");
                    return; // 요청 중단
                }
            } else if ("access".equals(tokenType)) {
                log.debug("JwtAuthFilter - 정식 토큰으로 요청");
                Long userId = tokenService.extractUserIdFromToken(token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("JwtAuthFilter - 알 수 없는 토큰 타입: {}", tokenType);
                sendForbiddenResponse(response, "잘못된 토큰입니다.");
                return; // 요청 중단
            }
        } catch (Exception e) {
            log.error("JwtAuthFilter - 토큰 검증 중 예외 발생: {}", e.getMessage());
            sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
            return; // 요청 중단
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

