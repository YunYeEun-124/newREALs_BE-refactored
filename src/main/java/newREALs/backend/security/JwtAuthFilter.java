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

        String token = tokenService.extractTokenFromHeader(request);

        try {
            if (token != null && tokenService.validateToken(token)) {
                String tokenType = tokenService.getTokenType(token);

                if ("temporary".equals(tokenType)) {
                    log.debug("JwtAuthFilter - 임시 토큰으로 요청");

                    // 임시 토큰일 경우 SecurityContext에 인증 정보 설정
                    String email = tokenService.extractEmailFromToken(token);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // /accounts/register 외의 API는 접근 차단
                    if (!request.getRequestURI().startsWith("/accounts/register")) {
                        log.warn("JwtAuthFilter - 임시 토큰으로 접근이 차단된 API 요청: {}", request.getRequestURI());
                        sendForbiddenResponse(response, "임시 토큰으로는 접근할 수 없습니다.");
                        return;
                    }
                } else if ("access".equals(tokenType)) {
                    log.debug("JwtAuthFilter - 정식 토큰으로 요청");
                    Long userId = tokenService.extractUserIdFromToken(token); // 정식 토큰일 경우만 userId 추출
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("JwtAuthFilter - 알 수 없는 토큰 타입: {}", tokenType);
                    sendForbiddenResponse(response, "잘못된 토큰입니다.");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("JwtAuthFilter - 토큰 검증 중 예외 발생: {}", e.getMessage());
            sendForbiddenResponse(response, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }


}

