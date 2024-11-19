package it.andrea.start.constants;

public interface ApplicationConstants {

    // Entità
    static final String SYSTEM_ENTITY_MANAGE = "SYSTEM";
    static final String ADMIN_ENTITY_MANAGE = "ADMIN";

    // Validità del token in giorni
    static final int TOKEN_VALIDITY_DAYS = 30;

    // Ruoli
    static final String ROLE_PREFIX = "ROLE_";
    static final String SYSTEM_ROLE_ADMIN = "ADMIN";
    static final String SYSTEM_ROLE_MANAGER = "MANAGER";
    static final String SYSTEM_ROLE_SUPERVISOR = "SUPERVISOR";
    static final String SYSTEM_ROLE_OPERATOR = "OPERATOR";
    static final String SYSTEM_SITE_WEB = "SITE_WEB";

    // Ruoli con annotazione
    static final String SYSTEM_ROLE_ADMIN_ANNOTATION = ROLE_PREFIX + SYSTEM_ROLE_ADMIN;
    static final String SYSTEM_ROLE_MANAGER_ANNOTATION = ROLE_PREFIX + SYSTEM_ROLE_MANAGER;
    static final String SYSTEM_ROLE_SUPERVISOR_ANNOTATION = ROLE_PREFIX + SYSTEM_ROLE_SUPERVISOR;
    static final String SYSTEM_ROLE_OPERATOR_ANNOTATION = ROLE_PREFIX + SYSTEM_ROLE_OPERATOR;
    static final String SYSTEM_SITE_WEB_ANNOTATION = ROLE_PREFIX + SYSTEM_SITE_WEB;

    // Lingue
    static final String DEFAULT_LANGUAGE = "it";
    static final String ENGLISH_LANGUAGE = "en";

    // Validità del token dell'utente in giorni
    static final int DAY_VALIDATION_TOKEN_USER = 3;

    // Ritardo standard in secondi per l'attivazione del trigger
    static final long SECOND_STANDARD_DELAY_START_TRIGGER = 5L;

    // Nomi dei job
    static final String INIZIALIZE_JOB = "InitializeJob";
    static final String INIZIALIZE_JOB_GROUP = INIZIALIZE_JOB + "_Group";

    // Criteria
    static final int MAX_PAGE_SIZE = 1000;

}
