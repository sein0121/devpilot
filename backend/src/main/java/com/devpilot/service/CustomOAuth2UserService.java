package com.devpilot.service;

import com.devpilot.domain.AuthProvider;
import com.devpilot.domain.User;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String githubId = String.valueOf(attributes.get("id"));
        String nickname = (String) attributes.getOrDefault("login", "github-user");
        
        String emailFromGithub = (String) attributes.get("email");
        final String email = (emailFromGithub == null || emailFromGithub.isBlank())
                ? nickname + "@github.local"
                : emailFromGithub;

        User user = userRepository.findByProviderAndProviderId(AuthProvider.GITHUB, githubId)
        .orElseGet(() -> userRepository.save(
                User.createGithubUser(email, nickname, githubId)
        ));
        
        return new DevPilotOAuth2User(user, attributes);
    }
}