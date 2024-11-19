package it.andrea.start.searchcriteria;

import java.io.Serializable;

public class SortSpecification implements Serializable {

    private static final long serialVersionUID = 991559548882915483L;

    private String field;
    private SortDirection direction;

    public SortSpecification() {
    }

    public SortSpecification(String field, SortDirection direction) {
        this.field = field;
        this.direction = direction;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SortDirection getDirection() {
        return this.direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

}
