package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final AccountsRepository accountsRepository;
    private final TokenService tokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    // applications.yml에서 값 가져오기
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    // 프론트엔드에서 받은 인가 코드 -> 카카오에서 AccessToken 받아와 -> 이걸로 사용자 정보 가져와 구현
    public Map<String, Object> processKakaoLogin(String authorizationCode) {
        // 인가코드로 AccessToken 받아와
        String accessToken = getAccessToken(authorizationCode);
        // 그걸로 사용자 정보 가져와
        Map<String, Object> userInfo = getUserInfo(accessToken);

        // 받아온 userInfo에서 필요한 정보 가져오기
        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String name = (String) ((Map<String, Object>) userInfo.get("properties")).get("nickname");
        String profilePath = (String) ((Map<String, Object>) userInfo.get("properties")).get("profile_image");

        // 리다이렉트 다르게 하기 위해 플래그 설정
        // findByEmail이 반환하는 객체가 비어있으면 true
        // => 신규계정(객체 비어있으면) true, 아니면 false
        boolean isNewAccount = accountsRepository.findByEmail(email).isEmpty();;

        Optional<Accounts> existingAccount = accountsRepository.findByEmail(email);
        Accounts account = existingAccount
                // 없으면 생성
                .orElseGet(() -> accountsRepository.save(
                Accounts.builder()
                        .name(name)
                        .email(email)
                        .profilePath(profilePath)
                        .build()
        ));

        // JWT 토큰 생성
        String jwtToken = tokenService.generateToken(account);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", jwtToken);
        response.put("isNewAccount", isNewAccount);
        response.put("name", account.getName());
        response.put("email", account.getEmail());
        response.put("userPk", account.getId());

        return response;
    }

    // Access Token 받아오기
    private String getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&code=" + authorizationCode;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        return (String) responseBody.get("access_token");
    }

    // 사용자 정보 가져오기
    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}
