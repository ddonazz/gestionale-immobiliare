package it.andrea.start.searchcriteria;

import java.io.Serializable;
import java.util.Collection;

public abstract class AbstractSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1357026826384915997L;

    private Collection<SortSpecification> sort;

    public Collection<SortSpecification> getSort() {
	return this.sort;
    }

    public void setSort(Collection<SortSpecification> sort) {
	this.sort = sort;
    }

}
