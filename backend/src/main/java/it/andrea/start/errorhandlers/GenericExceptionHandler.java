package it.andrea.start.errorhandlers;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import it.andrea.start.controller.response.InternalServerErrorResponse;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.RollbackException;

@ControllerAdvice
public class GenericExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionHandler.class);

    private static final String GENERIC_MESSAGE_KEY = "error.internalservererror";
    private static final String TRANSACTION_ROLLED_BACK_KEY = "error.transactionrolledback";
    private static final String PERSISTENCE_EXCEPTION_KEY = "error.persistenceexception";
    private static final String CONSTRAINT_VIOLATION_KEY = "error.constraintviolation";
    private static final String SQL_GENERICERROR_KEY = "error.sqlgeneric";
    private static final String SQL_ERROR_PREFIX = "error.sql";
    private static final String NULL_POINTER_KEY = "error.nullpointer";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final ResponseEntity<Object> toResponse(Exception exception, Locale locale) {
        LOG.error("Catch exception", exception);
        ResourceBundle rb = ResourceBundle.getBundle("bundles.Messages", locale);
		String messageKey = GENERIC_MESSAGE_KEY;
		if (exception instanceof RollbackException rollbackException) {
			exception = (Exception) rollbackException.getCause();
			messageKey = TRANSACTION_ROLLED_BACK_KEY;
		}
		else if (exception instanceof PersistenceException persistenceException) {
			exception = (Exception) persistenceException.getCause();
			messageKey = PERSISTENCE_EXCEPTION_KEY;
		}
		else if (exception instanceof ConstraintViolationException constraintViolationException) {
			exception = (Exception) constraintViolationException.getCause();
			messageKey = CONSTRAINT_VIOLATION_KEY;
		}
		else if (exception instanceof SQLException sqlException) {
			String key = SQL_ERROR_PREFIX + sqlException.getErrorCode();
			if (rb.containsKey(key)) {
				messageKey = key;
			} else {
				messageKey = SQL_GENERICERROR_KEY;
			}
		}
		else if (exception instanceof NullPointerException nullPointerException) {
			exception = (Exception) nullPointerException.getCause();
			messageKey = NULL_POINTER_KEY;
		}
    
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InternalServerErrorResponse(exception.getMessage(), rb.getString(messageKey)));
        
    }

}