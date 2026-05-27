package com.example.project.services.custom;

import com.example.project.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomOidcUser implements OidcUser {

    private final OidcUser delegate;
    private final User user;

    public CustomOidcUser(OidcUser delegate, User user) {
        this.delegate = delegate;
        this.user = user;
    }

    public User getUser() { return user; }
    public String getEmail() { return user.getEmail(); }

    @Override
    public Map<String, Object> getClaims() { return delegate.getClaims(); }

    @Override
    public OidcUserInfo getUserInfo() { return delegate.getUserInfo(); }

    @Override
    public OidcIdToken getIdToken() { return delegate.getIdToken(); }

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attrs = new HashMap<>(delegate.getAttributes());
        attrs.put("email",  user.getEmail());
        attrs.put("db_id",  user.getId().toString());
        attrs.put("role",   user.getRole().name());
        return attrs;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() != null) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
        }
        return delegate.getAuthorities();
    }

    @Override
    public String getName() { return user.getEmail(); }
}