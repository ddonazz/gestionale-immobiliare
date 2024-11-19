package it.andrea.start.errorhandlers;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import it.andrea.start.controller.response.BadRequestResponse;
import it.andrea.start.exception.user.UserAlreadyExistsException;
import it.andrea.start.exception.user.UserException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.exception.user.UserRoleAlreadyExistsException;
import it.andrea.start.exception.user.UserRoleNotFoundException;

@ControllerAdvice
public class UserExceptionHandler {

    private static final String ENTITY = "User";
    
    @ExceptionHandler(UserException.class)
    public final ResponseEntity<Object> toResponse(UserException userException, Locale locale) {
        ResourceBundle rb = ResourceBundle.getBundle("bundles.Messages", locale);
        String message = mapMessage(userException, rb);

        BadRequestResponse response = new BadRequestResponse(ENTITY, userException.getMessage(), message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String mapMessage(UserException userException, ResourceBundle rb) {
        if (userException instanceof UserAlreadyExistsException) {
            return rb.getString("error.user.alreadyexists");
        } 
        else if (userException instanceof UserNotFoundException) {
            return rb.getString("error.user.notfound");
        } 
        else if (userException instanceof UserRoleAlreadyExistsException) {
            return rb.getString("error.userrole.alreadyexists");
        } 
        else if (userException instanceof UserRoleNotFoundException) {
            return rb.getString("error.userrole.notfound");
        } 
        else {
            return userException.getMessage();
        }
    }
}