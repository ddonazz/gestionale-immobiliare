package it.andrea.start.models.support;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum AuditActivity {
    
    ANONYMOUS,
    ANONYMOUS_EXCEPTION,
    USER_OPERATION,
    USER_OPERATION_EXCEPTION,
    QUARTZ_OPERATION,
    SYSTEM_RESTART
    
}
