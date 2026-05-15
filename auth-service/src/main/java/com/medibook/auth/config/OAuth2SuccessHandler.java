package com.medibook.auth.config;

import com.medibook.auth.dto.LoginResponse;
import com.medibook.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        log.info("Google OAuth2 login successful for: {}", email);

        try {
            LoginResponse loginResponse = authService.processOAuthLogin(email, name, picture);

            String redirectUrl = String.format(
                    "%s/oauth-callback?token=%s&refreshToken=%s&userId=%d&name=%s&email=%s&role=%s&profilePicture=%s",
                    frontendUrl,
                    URLEncoder.encode(loginResponse.getToken(), StandardCharsets.UTF_8),
                    URLEncoder.encode(loginResponse.getRefreshToken(), StandardCharsets.UTF_8),
                    loginResponse.getUserId(),
                    URLEncoder.encode(loginResponse.getName(), StandardCharsets.UTF_8),
                    URLEncoder.encode(loginResponse.getEmail(), StandardCharsets.UTF_8),
                    URLEncoder.encode(loginResponse.getRole(), StandardCharsets.UTF_8),
                    URLEncoder.encode(loginResponse.getProfilePicture() != null ? loginResponse.getProfilePicture() : "", StandardCharsets.UTF_8)
            );

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 login failed: {}", e.getMessage());
            String errorUrl = frontendUrl + "/login?error=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}
