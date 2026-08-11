package com.devpilot.global.security;

import com.devpilot.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class DevPilotOAuth2User implements OAuth2User {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public DevPilotOAuth2User(User user, Map<String, Object> attributes) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.attributes = attributes;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}