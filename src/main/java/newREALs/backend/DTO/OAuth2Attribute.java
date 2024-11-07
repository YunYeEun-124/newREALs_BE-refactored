package newREALs.backend.DTO;

// 사용자 정보 추출하여 OAuth2Attribute 객체로 관리할 수 있게 해줌
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.domain.Accounts;

import java.util.Map;

@SuppressWarnings("unchecked")
@Slf4j
@Getter
public class OAuth2Attribute {
    private final String name;
    private final String email;
    private final String profilePath;
    private final Map<String, Object> attributes;

    @Builder
    public OAuth2Attribute(String name, String email, String profilePath, Map<String, Object> attributes) {
        this.name = name;
        this.email = email;
        this.profilePath = profilePath;
        this.attributes = attributes;
    }

    // 파싱
    public static OAuth2Attribute ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // 사용자 프로필 정보 추출
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String email = (String) kakaoAccount.get("email");
        String name = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");

        log.info("Kakao Account Info: Email = {}, Name = {}, Profile Image = {}", email, name, profileImage);


        return OAuth2Attribute.builder()
                .name(name)
                .email(email)
                .profilePath(profileImage)
                .attributes(attributes)
                .build();
    }

    public Accounts toEntity() {
        return Accounts.builder()
                .name(this.name)
                .email(this.email)
                .profilePath(this.profilePath)
                .build();
    }
}