package it.andrea.start.security.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import it.andrea.start.constants.UserStatus;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.models.user.User;
import it.andrea.start.support.UserAccountData;

public class JWTokenUserDetails extends UserDTO implements UserDetails {

    private static final long serialVersionUID = -6774255085053174788L;

    private String password;
    private UserAccountData userAccountData;
    private LocalDateTime issuedAt;
    private LocalDateTime expiration;
    private Collection<? extends GrantedAuthority> authorities;

    public JWTokenUserDetails(Long id, String username, String password, String name, String email, UserStatus userStatus, Collection<UserRoleDTO> roles, Collection<? extends GrantedAuthority> authorities, UserAccountData userAccountData) {
        this.setId(id); 
        this.setUsername(username);
        this.password = password;
        this.setName(name);
        this.setEmail(email);
        this.setUserStatus(userStatus);
        this.setRoles(roles);
        this.authorities = authorities;
        this.userAccountData = userAccountData;
    }

    public static JWTokenUserDetails build(User user, UserAccountData userAccountData) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole())).collect(Collectors.toList());
        List<UserRoleDTO> roles = user.getRoles().stream().map(role -> new UserRoleDTO(role.getRole(), role.getRoleDescription())).toList();

        return new JWTokenUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getName(), user.getEmail(), user.getUserStatus(), roles, authorities, userAccountData);
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public UserAccountData getUserAccountData() {
        return userAccountData;
    }

    public void setUserAccountData(UserAccountData userAccountData) {
        this.userAccountData = userAccountData;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JWTokenUserDetails user = (JWTokenUserDetails) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
