package com.T82.coupon.global.domain.dto;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class UserDto implements UserDetails {

    private final String id;
    private final String email;
    private final String name;

    public static UserDto fromClaims(Claims claims){
        String id = claims.get("id", String.class);
        String name = claims.get("name", String.class);
        String email = claims.get("email", String.class);
        return new UserDto(id, name, email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
