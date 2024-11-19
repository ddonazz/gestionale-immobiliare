package it.andrea.start.searchcriteria.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import it.andrea.start.searchcriteria.AbstractSearchCriteria;
import it.andrea.start.searchcriteria.specification.ElementSearchCriteria;
import it.andrea.start.searchcriteria.specification.SearchOperation;

public class UserRoleSearchCriteria extends AbstractSearchCriteria {

    private static final long serialVersionUID = 5040053432580651652L;

    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<ElementSearchCriteria> getSearchCriteria() {

        List<ElementSearchCriteria> criterias = new ArrayList<ElementSearchCriteria>();

        if (StringUtils.isNotBlank(roleName)) {
            ElementSearchCriteria criteria = new ElementSearchCriteria();
            criteria.setKey("role");
            criteria.setOperation(SearchOperation.LIKE_NO_CASE_SENSITY);
            criteria.setValue(this.roleName);
            criterias.add(criteria);
        }

        return criterias;
    }

}
