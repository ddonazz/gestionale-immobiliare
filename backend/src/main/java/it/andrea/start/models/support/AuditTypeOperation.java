package it.andrea.start.models.support;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum AuditTypeOperation {
    
    INITIALIZE,
    LOGIN,
    LOGIN_API_WEB,
    API_WEB,
    GET_INFO,
    QUARTZ,
    CREATE,
    UPDATE,
    DELETE,
    EMAIL_SEND

}
