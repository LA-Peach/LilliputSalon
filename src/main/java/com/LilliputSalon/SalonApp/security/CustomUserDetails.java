package com.LilliputSalon.SalonApp.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.LilliputSalon.SalonApp.domain.User;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getProfile() == null) {
            System.out.println("ERROR: Profile is null for " + user.getEmail());
            return Set.of();
        }

        if (user.getProfile().getUserType() == null) {
            System.out.println("ERROR: UserType is null for " + user.getEmail());
            return Set.of();
        }

        String rawRole = user.getProfile().getUserType().getTypeName();
        System.out.println("User TypeName from DB: " + rawRole);

        String role = rawRole.toUpperCase();
        String finalRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        System.out.println("Final assigned authority: " + finalRole);
        return Set.of(new SimpleGrantedAuthority(finalRole));
    }



    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

}
