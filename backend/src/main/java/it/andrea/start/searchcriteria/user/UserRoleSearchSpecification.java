package it.andrea.start.searchcriteria.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import it.andrea.start.models.user.UserRole;
import it.andrea.start.searchcriteria.specification.ElementSearchCriteria;
import it.andrea.start.searchcriteria.specification.SearchOperation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class UserRoleSearchSpecification implements Specification<UserRole> {

    private static final long serialVersionUID = -455893825245726055L;

    private List<ElementSearchCriteria> criterias;

    public UserRoleSearchSpecification(List<ElementSearchCriteria> criterias) {
        this.criterias = criterias;
    }

    @Override
    public Predicate toPredicate(Root<UserRole> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        // create a new predicate list
        List<Predicate> predicatesAnd = new ArrayList<Predicate>();
        List<Predicate> predicatesOr = new ArrayList<Predicate>();

        if (criterias != null && !criterias.isEmpty()) {

            // add add criteria to predicates
            for (ElementSearchCriteria criteria : criterias) {

                if (criteria.getOperation().equals(SearchOperation.EQUALITY)) {
                    predicatesAnd.add(criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue().toString()));

                } else if (criteria.getOperation().equals(SearchOperation.NEGATION)) {
                    predicatesAnd.add(criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue().toString()));

                } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN)) {
                    predicatesAnd.add(criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));

                } else if (criteria.getOperation().equals(SearchOperation.GREATER_THAN)) {
                    predicatesAnd.add(criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));

                } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN_OR_EQUAL)) {
                    predicatesAnd.add(criteriaBuilder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));

                } else if (criteria.getOperation().equals(SearchOperation.GREATER_THAN_OR_EQUAL)) {
                    predicatesAnd.add(criteriaBuilder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));

                } else if (criteria.getOperation().equals(SearchOperation.LIKE_CASE_SENSITY)) {
                    String value = "%" + criteria.getValue().toString() + "%";
                    predicatesOr.add(criteriaBuilder.like(root.get(criteria.getKey()), value));

                } else if (criteria.getOperation().equals(SearchOperation.LIKE_NO_CASE_SENSITY)) {
                    String value = "%" + criteria.getValue().toString() + "%";
                    predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get(criteria.getKey())), value.toUpperCase()));
                }
            }

            Predicate orPredicate = criteriaBuilder.or(predicatesOr.toArray(new Predicate[0]));
            predicatesAnd.add(orPredicate);
        }

        return criteriaBuilder.and(predicatesAnd.toArray(new Predicate[0]));
    }

}
