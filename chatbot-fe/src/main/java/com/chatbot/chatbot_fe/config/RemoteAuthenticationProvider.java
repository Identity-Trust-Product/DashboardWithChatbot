package com.chatbot.chatbot_fe.config;

import com.chatbot.chatbot_fe.service.AuthProxyService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RemoteAuthenticationProvider implements AuthenticationProvider {

    private final AuthProxyService authProxy;

    public RemoteAuthenticationProvider(AuthProxyService authProxy) {
        this.authProxy = authProxy;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        try {
            Map<String, Object> user = authProxy.login(username, password);
            String role = String.valueOf(user.getOrDefault("role", "USER"));
            if (role.isBlank() || "null".equalsIgnoreCase(role)) role = "USER";
            return new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}