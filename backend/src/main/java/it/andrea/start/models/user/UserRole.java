package it.andrea.start.models.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "user_role", 
        uniqueConstraints = { @UniqueConstraint(columnNames = "role") }, 
        indexes = { @Index(name = "IDX_ROLE", columnList = "role"), })
public class UserRole {

    @Id
    private String role;

    @Column(nullable = false)
    private String roleDescription;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

}