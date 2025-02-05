package it.andrea.start.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
	description = "", 
	method = "", 
	summary = "", 
	responses = { 
		@ApiResponse(responseCode = "200", description = "OK"), 
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), 
		@ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), 
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), 
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
		}
	)
public @interface CustomApiOperation {

    String description() default "No description provided";

    String method() default "POST";

    String summary() default "No summary provided";
}