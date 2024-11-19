package it.andrea.start.searchcriteria.audit;

import java.time.LocalDateTime;

import it.andrea.start.models.support.AuditActivity;
import it.andrea.start.models.support.AuditTypeOperation;
import it.andrea.start.searchcriteria.AbstractSearchCriteria;

public class AuditTraceSearchCriteria extends AbstractSearchCriteria {

    private static final long serialVersionUID = -7357362716606684198L;

    private Long id;
    private String sessionId;
    private AuditActivity activity;
    private Long userId;
    private String userName;
    private AuditTypeOperation auditType;
    private String textSearch;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public AuditActivity getActivity() {
        return activity;
    }

    public void setActivity(AuditActivity activity) {
        this.activity = activity;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public AuditTypeOperation getAuditType() {
        return auditType;
    }

    public void setAuditType(AuditTypeOperation auditType) {
        this.auditType = auditType;
    }

    public String getTextSearch() {
        return textSearch;
    }

    public void setTextSearch(String textSearch) {
        this.textSearch = textSearch;
    }

    public LocalDateTime getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDateTime getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
    }

}
