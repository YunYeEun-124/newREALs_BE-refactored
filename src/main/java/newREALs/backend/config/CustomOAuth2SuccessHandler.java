package newREALs.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.AccountsRepository;
import newREALs.backend.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final AccountsRepository accountsRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.debug("CustomOAuth2SuccessHandler -> onAuthenticationSuccess() 호출");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");

        // Access Token 생성
        String accessToken = tokenService.generateToken(name, email);

        // Access Token을 헤더에 추가하여 프론트엔드에 전달
        response.setHeader("Authorization", "Bearer " + accessToken);

        Optional<Accounts> existingAccount = accountsRepository.findByEmail(email);
        String redirectUrl;

        if (existingAccount.isPresent()) {
            // 기존 사용자 -> /home으로 리다이렉트
            redirectUrl = "/home";
        } else {
            // 신규 사용자 -> /register로 리다이렉트
            redirectUrl = "/register";
        }
        log.debug("Redirecting to: {}", redirectUrl);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
