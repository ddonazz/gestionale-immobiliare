package it.andrea.start.mappers;

import java.util.ArrayList;
import java.util.Collection;

import it.andrea.start.exception.MappingToDtoException;
import jakarta.persistence.EntityManager;

public abstract class AbstractMapper<T, E> implements Mapper<T, E> {

    private EntityManager entityManager;

    protected AbstractMapper(EntityManager entityManager) {
	super();
	this.entityManager = entityManager;
    }

    public final Collection<T> toDtos(Collection<E> elements) throws MappingToDtoException {
	Collection<T> result = new ArrayList<>();
	if (elements != null) {
	    for (E element : elements) {
		if (element != null) {
		    result.add(toDto(element));
		}
	    }
	}
	return result;
    }

    protected EntityManager getEntityManager() {
	return this.entityManager;
    }

}
