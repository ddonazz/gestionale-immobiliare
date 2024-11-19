package it.andrea.start.models.user;

import java.util.Collection;

import it.andrea.start.constants.UserStatus;
import it.andrea.start.models.BaseEntityLong;
import it.andrea.start.models.FirstBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;

@Entity
@Table(
        name = "users", 
        uniqueConstraints = {
        		@UniqueConstraint(columnNames = "username") 
        	}, 
        indexes = {
                @Index(name = "IDX_USER_USERNAME", columnList = "username"),
                @Index(name = "IDX_USER_EMAIL", columnList = "email"),
                @Index(name = "IDX_USER_STATUS", columnList = "username, userStatus")
        	}
        )
public class User extends FirstBaseEntity implements BaseEntityLong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Email
    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    @Column
    private String languageDefault;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role_user", 
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), 
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role"))
    private Collection<UserRole> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Collection<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Collection<UserRole> roles) {
        this.roles = roles;
    }

    public String getLanguageDefault() {
        return languageDefault;
    }

    public void setLanguageDefault(String languageDefault) {
        this.languageDefault = languageDefault;
    }

}
