package it.andrea.start.support;

import java.io.Serializable;
import java.util.Collection;

import it.andrea.start.dto.user.UserRoleDTO;

public class UserAccountData implements Serializable {

    private static final long serialVersionUID = 7547411746162816358L;

    private String email;
    private Collection<UserRoleDTO> roles;
    private String languageDefault;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Collection<UserRoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Collection<UserRoleDTO> roles) {
        this.roles = roles;
    }

    public String getLanguageDefault() {
        return languageDefault;
    }

    public void setLanguageDefault(String languageDefault) {
        this.languageDefault = languageDefault;
    }

}
