package newREALs.backend.service;

// OAuth2 구현하기..
// 새로운 계정 생성 or 기존 계정 반환 처리

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.DTO.OAuth2Attribute;
import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.AccountsRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final AccountsRepository accountsRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // 사용자의 정보를 OAuth2User 타입으로 받아옴

        OAuth2Attribute attributes = OAuth2Attribute.ofKakao(oAuth2User.getAttributes());

        log.info("User attributes: {}", oAuth2User.getAttributes());

        // 이미 존재하는 계정인지 확인
        String email = attributes.getEmail();
        Accounts account = accountsRepository.findByEmail(email)
                .orElseGet(() -> registerNewAccount(attributes)); // 존재하는 계정 없으면 -> 계정 생성

        return new CustomOAuth2UserServiceImpl(account, attributes.getAttributes());
    }

    private Accounts registerNewAccount(OAuth2Attribute attributes) {
        return accountsRepository.save(attributes.toEntity());
    }

}