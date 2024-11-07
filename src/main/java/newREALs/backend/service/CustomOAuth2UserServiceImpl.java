package newREALs.backend.service;

// 구현체

import lombok.Getter;
import newREALs.backend.domain.Accounts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2UserServiceImpl implements OAuth2User {
    @Getter
    private final Accounts account;
    private final Map<String, Object> attributes;

    public CustomOAuth2UserServiceImpl(Accounts account, Map<String, Object> attributes) {
        this.account = account;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return account.getName();
    }

    public String getEmail() {
        return account.getEmail();
    }

    // 권한 부여 -> ROLE_USER? 일단 다 null로 처리..
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

}