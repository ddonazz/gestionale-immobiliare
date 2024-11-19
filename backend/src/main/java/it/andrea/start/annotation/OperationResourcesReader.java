package it.andrea.start.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;
import it.andrea.start.constants.CustomerStatus;
import it.andrea.start.constants.Gender;
import it.andrea.start.constants.MediaType;
import it.andrea.start.constants.MessageType;
import it.andrea.start.constants.TypeRegistry;
import it.andrea.start.constants.UnitTime;
import it.andrea.start.constants.UserStatus;

@Component
public class OperationResourcesReader implements OperationCustomizer {

    private final Map<Class<?>, Function<HandlerMethod, String>> annotationHandlers;

    public OperationResourcesReader() {
        annotationHandlers = new HashMap<>();
        annotationHandlers.put(ApiRoleAccessNotes.class, this::processRoleAccessNotes);
        annotationHandlers.put(ApiUserStatusAccessNotes.class, this::processUserStatusAccessNotes);
        annotationHandlers.put(ApiMessageTypeAccessNotes.class, this::processMessageTypeAccessNotes);
        annotationHandlers.put(ApiCustomerStatusAccessNotes.class, this::processCustomerStatusAccessNotes);
        annotationHandlers.put(ApiTypeRegistryAccessNotes.class, this::processTypeRegistryAccessNotes);
        annotationHandlers.put(ApiGenderAccessNotes.class, this::processGenderAccessNotes);
        annotationHandlers.put(ApiUnitTimeAccessNotes.class, this::processUnitTimeAccessNotes);
        annotationHandlers.put(ApiMediaTypeAccessNotes.class, this::processMediaTypeAccessNotes);
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        String description = operation.getDescription();
        for (Map.Entry<Class<?>, Function<HandlerMethod, String>> entry : annotationHandlers.entrySet()) {
            Function<HandlerMethod, String> handler = entry.getValue();
            String annotationInfo = handler.apply(handlerMethod);
            if (annotationInfo != null && !annotationInfo.isEmpty()) {
                description += "</br></br>" + annotationInfo;
            }
        }
        operation.setDescription(description);
        return operation;
    }

    private String processRoleAccessNotes(HandlerMethod handlerMethod) {
        ApiRoleAccessNotes annotationRole = handlerMethod.getMethodAnnotation(ApiRoleAccessNotes.class);
        return annotationRole != null ? "Roles permitted : " + String.join(", ", annotationRole.roles()) : null;
    }

    private String processUserStatusAccessNotes(HandlerMethod handlerMethod) {
        ApiUserStatusAccessNotes annotationUserStatus = handlerMethod.getMethodAnnotation(ApiUserStatusAccessNotes.class);
        return annotationUserStatus != null ? "UserStatus = " + UserStatus.values() : null;
    }

    private String processMessageTypeAccessNotes(HandlerMethod handlerMethod) {
        ApiMessageTypeAccessNotes annotationMessageType = handlerMethod.getMethodAnnotation(ApiMessageTypeAccessNotes.class);
        return annotationMessageType != null ? "MessageType = " + MessageType.values() : null;
    }

    private String processCustomerStatusAccessNotes(HandlerMethod handlerMethod) {
        ApiCustomerStatusAccessNotes annotationCustomerStatus = handlerMethod.getMethodAnnotation(ApiCustomerStatusAccessNotes.class);
        return annotationCustomerStatus != null ? "CustomerStatus = " + CustomerStatus.values() : null;
    }

    private String processTypeRegistryAccessNotes(HandlerMethod handlerMethod) {
        ApiTypeRegistryAccessNotes annotationTypeRegistry = handlerMethod.getMethodAnnotation(ApiTypeRegistryAccessNotes.class);
        return annotationTypeRegistry != null ? "TypeRegistry = " + TypeRegistry.values() : null;
    }

    private String processGenderAccessNotes(HandlerMethod handlerMethod) {
        ApiGenderAccessNotes annotationGender = handlerMethod.getMethodAnnotation(ApiGenderAccessNotes.class);
        return annotationGender != null ? "Gender = " + Gender.values() : null;
    }

    private String processUnitTimeAccessNotes(HandlerMethod handlerMethod) {
        ApiUnitTimeAccessNotes annotationUnitTime = handlerMethod.getMethodAnnotation(ApiUnitTimeAccessNotes.class);
        return annotationUnitTime != null ? "UnitTime = " + UnitTime.values() : null;
    }

    private String processMediaTypeAccessNotes(HandlerMethod handlerMethod) {
        ApiMediaTypeAccessNotes annotationMediaType = handlerMethod.getMethodAnnotation(ApiMediaTypeAccessNotes.class);
        if (annotationMediaType != null) {
            List<String> mediaTypeValues = MediaType.stream().map(mediaType -> "- " + mediaType.name() + " , " + mediaType.toString()).collect(Collectors.toList());
            return "Media type possible value = </br>" + String.join("</br>", mediaTypeValues);
        }
        return null;
    }

}
