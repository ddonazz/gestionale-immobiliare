package it.andrea.start.dto.user;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRoleDTO implements Serializable {

	private static final long serialVersionUID = 4014378396482783842L;

	@NotBlank(message = "Il ruolo non può essere vuoto")
	@Size(min = 5, max = 60, message = "Il ruolo avere almeno 5 e massimo 60 caratteri")
	private String role;

	@NotBlank(message = "La descrizione del ruolo non può essere vuoto")
	@Size(min = 3, max = 255, message = "La descrizione del ruolo deve avere almeno 3 e massimo 255 caratteri")
	private String roleDescription;

	public UserRoleDTO() {
		super();
	}

	public UserRoleDTO(String role, String roleDescription) {
		super();
		this.role = role;
		this.roleDescription = roleDescription;
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(role);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof UserRoleDTO))
			return false;
		UserRoleDTO other = (UserRoleDTO) obj;
		return Objects.equals(role, other.role);
	}

}
