package com.booking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
        // hide constructor
    }

    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority(role));
    }

    public static boolean hasRole(String role) {
        return hasRole(SecurityContextHolder.getContext().getAuthentication(), role);
    }
}
