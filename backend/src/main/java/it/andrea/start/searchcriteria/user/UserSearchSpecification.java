package it.andrea.start.searchcriteria.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import it.andrea.start.constants.UserStatus;
import it.andrea.start.models.user.User;
import it.andrea.start.models.user.UserRole;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class UserSearchSpecification implements Specification<User> {

    private static final long serialVersionUID = -1987604702637357646L;

    private UserSearchCriteria criterias;

    public UserSearchSpecification(UserSearchCriteria criterias) {
        this.criterias = criterias;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        Long id = criterias.getId();
        String username = criterias.getUsername();
        String textSearch = criterias.getTextSearch();
        UserStatus userStatus = criterias.getUserStatus();
        String[] role = criterias.getRole();
        String[] roleNotValid = criterias.getRoleNotValid();

        if (id != null) {
            predicates.add(criteriaBuilder.equal(root.get("id"), id));
        }
        if (StringUtils.isNotBlank(username)) {
            predicates.add(criteriaBuilder.equal(root.get("username"), username));
        }
        if (StringUtils.isNotBlank(textSearch)) {
            String value = "%" + textSearch + "%";
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.upper(root.get("username")), value.toUpperCase()), 
                    criteriaBuilder.like(criteriaBuilder.upper(root.get("email")), value.toUpperCase()), 
                    criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), value.toUpperCase())
                    ));
        }
        if (userStatus != null) {
            predicates.add(criteriaBuilder.equal(root.get("userStatus"), userStatus));
        }
        if (role != null && role.length > 0) {
            Join<User, UserRole> joinRole = root.join("roles");
            predicates.add(joinRole.get("role").in((Object[]) role));
        }
        if (roleNotValid != null && roleNotValid.length > 0) {
            Join<User, UserRole> joinRoleNot = root.join("roles");
            predicates.add(criteriaBuilder.not(joinRoleNot.get("role").in((Object[]) roleNotValid)));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
