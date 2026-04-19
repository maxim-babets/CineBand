package com.cineband.api.auth;

import com.cineband.api.domain.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Integer id;
    private final String email;
    private final String nick;
    private final String displayName;
    private final String passwordHash;

    public UserPrincipal(Integer id, String email, String nick, String displayName, String passwordHash) {
        this.id = id;
        this.email = email;
        this.nick = nick;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    public static UserPrincipal from(UserAccount u) {
        return new UserPrincipal(
                u.getId(),
                u.getEmail(),
                u.getNick(),
                u.getDisplayName(),
                u.getPasswordHash()
        );
    }

    public Integer getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
