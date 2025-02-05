package it.andrea.start.searchcriteria.user;

import it.andrea.start.constants.UserStatus;
import it.andrea.start.searchcriteria.AbstractSearchCriteria;

public class UserSearchCriteria extends AbstractSearchCriteria {

    private static final long serialVersionUID = 5296416720523897796L;

    private Long id;
    private String username;
    private String textSearch;
    private UserStatus userStatus;
    private String[] role;
    private String[] roleNotValid;

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

    public String getTextSearch() {
	return textSearch;
    }

    public void setTextSearch(String textSearch) {
	this.textSearch = textSearch;
    }

    public UserStatus getUserStatus() {
	return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
	this.userStatus = userStatus;
    }

    public String[] getRole() {
	return role;
    }

    public void setRole(String[] role) {
	this.role = role;
    }

    public String[] getRoleNotValid() {
	return roleNotValid;
    }

    public void setRoleNotValid(String[] roleNotValid) {
	this.roleNotValid = roleNotValid;
    }

}
