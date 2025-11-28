package com.epam.rd.autocode.spring.project.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class SecurityUser extends User {
    private final String fullName;

    public SecurityUser(String username, String password, boolean enabled,
                        boolean accountNonExpired, boolean credentialsNonExpired,
                        boolean accountNonLocked,
                        Collection<? extends GrantedAuthority> authorities, String fullName) {

        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.fullName = fullName;
    }
}
