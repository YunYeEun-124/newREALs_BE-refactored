package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class

KakaoService {
    private final UserRepository userRepository;
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



    public Map<String, Object> processKakaoLogin(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        Map<String, Object> userInfo = getUserInfo(accessToken);


        String email = Optional.ofNullable((String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email"))
                .orElseThrow(() -> new IllegalArgumentException("이메일 정보가 없습니다."));

        String name = (String) ((Map<String, Object>) userInfo.get("properties")).get("nickname");
        String profilePath = (String) ((Map<String, Object>) userInfo.get("properties")).get("profile_image");

        Optional<Accounts> existingAccount = userRepository.findByEmail(email);
        Accounts account = null;
        Map<String, Object> response = new HashMap<>();


        if (existingAccount.isPresent()) {
            //유저 존재 (관심사 등록까지 마침) - accessToken,refreshToken 전달
            account = existingAccount.get();
        } else {
            //임시토큰만 발급해서 리턴. 유저정보 저장X
            response.put("isNewAccount",true);
            response.put("tempToken",tokenService.generateTemporaryToken(email,name,profilePath));
            response.put("email",email);
            response.put("name",name);
            response.put("profilePath",profilePath);
            return response;
        }

        String jwtToken = tokenService.generateAccessToken(account);
        String refreshToken=tokenService.generateRefreshToken(account);
        response.put("isNewAccount",false);
        response.put("name", account.getName());
        response.put("email", account.getEmail());
        response.put("userId", account.getId());
        response.put("accessToken", jwtToken);
        response.put("refreshToken", refreshToken);

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
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        assert responseBody != null;
        return (String) responseBody.get("access_token");
    }

    // 사용자 정보 가져오기
    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }
}