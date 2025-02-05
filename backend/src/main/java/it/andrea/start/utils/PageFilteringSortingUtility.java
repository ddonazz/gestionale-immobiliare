package it.andrea.start.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import it.andrea.start.searchcriteria.AbstractSearchCriteria;
import it.andrea.start.searchcriteria.SortDirection;
import it.andrea.start.searchcriteria.SortSpecification;

public class PageFilteringSortingUtility {

    private PageFilteringSortingUtility() {
	super();
    }

    public static <T extends Serializable> void computePage(PagedResult<T> page, int itemCount, Integer pageNum, Integer pageSize) {
	page.setItemCount(itemCount);

	if (pageNum == null || pageSize == null) {
	    page.setPageCount(1);
	    page.setItemStart(null);
	    page.setItemNum(null);
	    page.setPageNum(null);
	    return;
	}

	int pageCount = (int) Math.ceil((double) itemCount / pageSize);
	page.setPageCount(pageCount);

	int start = (pageNum - 1) * pageSize;
	int tail = Math.min(itemCount - start, pageSize);
	if (start < itemCount) {
	    page.setItemStart(start);
	    page.setItemNum(tail);
	    page.setPageNum(pageNum);
	} else {
	    page.setItemStart(null);
	    page.setItemNum(null);
	    page.setPageNum(null);
	}
    }

    public static void applySort(AbstractSearchCriteria searchCriteria, String[] sorts) {
	Collection<SortSpecification> sortSpecifications = new ArrayList<>();
	if (sorts != null && sorts.length > 0) {
	    for (String sort : sorts) {
		String[] parts = sort.split("\\|");
		String field = parts[0].trim();
		SortDirection sortDirection = SortDirection.ASC;
		if (parts.length > 1 && SortDirection.DESC.name().equalsIgnoreCase(parts[1].trim())) {
		    sortDirection = SortDirection.DESC;
		}
		sortSpecifications.add(new SortSpecification(field, sortDirection));
	    }
	}
	searchCriteria.setSort(sortSpecifications);
    }

    public static List<Sort> generateSortList(Collection<SortSpecification> sortCriteria) {
	return sortCriteria.stream().map(criterion -> {
	    Direction direction = (criterion.getDirection() == SortDirection.DESC) ? Direction.DESC : Direction.ASC;
	    return Sort.by(direction, criterion.getField());
	}).toList();
    }

    public static Optional<Sort> getSortSequence(List<Sort> criteria) {
	return criteria.stream().reduce(Sort::and);
    }
}
