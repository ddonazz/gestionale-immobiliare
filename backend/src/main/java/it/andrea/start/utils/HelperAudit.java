package it.andrea.start.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.andrea.start.configuration.GlobalConfig;
import it.andrea.start.constants.AuditLevel;
import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.models.BaseEntityLong;
import it.andrea.start.models.BaseEntityString;
import it.andrea.start.models.support.AuditActivity;
import it.andrea.start.models.support.AuditTypeOperation;
import it.andrea.start.security.service.JWTokenUserDetails;
import jakarta.servlet.http.HttpServletRequest;

public class HelperAudit {
	
	private HelperAudit() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static AuditTraceDTO getAuditControllerOperation(AuditLevel auditLevel, AuditActivity auditActivity, JWTokenUserDetails jWTokenUserDetails, AuditTypeOperation auditTypeOperation, HttpServletRequest httpServletRequest, Object body, String controllerMethod) {
        if (auditLevel == AuditLevel.NOTHING || auditLevel == AuditLevel.DATABASE) {
            return null;
        }

        AuditTraceDTO auditTraceDTO = new AuditTraceDTO();
        auditTraceDTO.setSessionId(UUID.randomUUID().toString());
        auditTraceDTO.setActivity(auditActivity);
        auditTraceDTO.setDateEvent(LocalDateTime.now());
        auditTraceDTO.setAuditType(auditTypeOperation);
        auditTraceDTO.setControllerMethod(controllerMethod);

        if (jWTokenUserDetails != null) {
            auditTraceDTO.setUserId(jWTokenUserDetails.getId());
            auditTraceDTO.setUserName(jWTokenUserDetails.getUsername());
        }

        setHttpRequestInfo(httpServletRequest, auditTraceDTO, body);

        return auditTraceDTO;
    }

    private static void setHttpRequestInfo(HttpServletRequest request, AuditTraceDTO auditTraceDTO, Object body) {
        auditTraceDTO.setMethod(Optional.ofNullable(request.getMethod()).orElse(""));
        auditTraceDTO.setUrl(request.getRequestURI());

        Map<String, Object> parametersMap = new HashMap<>();
        extractRequestDetails(request, parametersMap);

        parametersMap.put("body", HelperString.toJson(body));

        auditTraceDTO.setHttpContextRequest(HelperString.toJson(parametersMap));
    }

    private static void extractRequestDetails(HttpServletRequest request, Map<String, Object> parametersMap) {
        String queryString = request.getQueryString();
        parametersMap.put("queryString", queryString != null ? queryString : null);
        parametersMap.put("parameterMap", request.getParameterMap());
        parametersMap.put("headers", Collections.list(request.getHeaderNames()).stream().collect(Collectors.toMap(h -> h, request::getHeader)));
        String ipCall = Optional.ofNullable(request.getHeader("X-Forwarded-For")).orElseGet(request::getRemoteAddr);
        parametersMap.put("ipCall", ipCall);
    }

    public static String getBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream inputStream = request.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            char[] charBuffer = new char[128];
            int bytesRead;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw e;
        }

        return stringBuilder.toString();
    }

    public static void auditControllerOperationAddResponseAndException(AuditTraceDTO auditTraceDTO, Object response, Exception ex) {
        if (auditTraceDTO == null) {
            return;
        }
        auditTraceDTO.setHttpContextResponse(HelperString.toJson(response));

        if (ex != null) {
            auditTraceDTO.setExceptionTrace(getStackTrace(ex));
        }
    }

    private static String getStackTrace(Exception ex) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            ex.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Failed to capture stack trace";
        }
    }

    public static void auditUnionsAndSettings(AuditTraceDTO auditTraceDTO, Collection<AuditTraceDTO> audits) {
        for (AuditTraceDTO item : audits) {
            item.setSessionId(auditTraceDTO.getSessionId());
            item.setActivity(auditTraceDTO.getActivity());
            item.setUserId(auditTraceDTO.getUserId());
            item.setUserName(auditTraceDTO.getUserName());
            item.setAuditType(auditTraceDTO.getAuditType());
        }

        audits.add(auditTraceDTO);
    }

    public static String getKeyLongValue(Long id) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("key", id);

        return HelperString.toJson(rootNode);
    }

    public static String getKeyStringValue(String id) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("key", id);

        return HelperString.toJson(rootNode);
    }

    public static String getKeyStringValue(Object id) {
        ObjectMapper mapper = new ObjectMapper();

        String jsonString = null;
        String jsonObject = HelperString.toJson(id);

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("key", jsonObject);

        jsonString = HelperString.toJson(rootNode);

        return jsonString;
    }

    public static <T extends BaseEntityLong> Collection<AuditTraceDTO> generateAudit(Collection<AuditTraceDTO> audits, GlobalConfig globalConfig, T entity, String entityName, String oldValue) {
        if (globalConfig.getAuditLevel() == AuditLevel.ALL || globalConfig.getAuditLevel() == AuditLevel.DATABASE) {
            AuditTraceDTO audit = new AuditTraceDTO();
            audit.setDateEvent(LocalDateTime.now());
            String keyValue = HelperAudit.getKeyLongValue(entity.getId());
            audit.setEntityName(entityName);
            audit.setEntityKeyValue(keyValue);
            audit.setEntityOldValue(oldValue);
            audit.setEntityNewValue(HelperString.toJson(entity));

            audits.add(audit);
        }
        return audits;
    }

    public static <T extends BaseEntityString> Collection<AuditTraceDTO> generateAudit(Collection<AuditTraceDTO> audits, GlobalConfig globalConfig, T entity, String entityName, String oldValue) {
        if (globalConfig.getAuditLevel() == AuditLevel.ALL || globalConfig.getAuditLevel() == AuditLevel.DATABASE) {
            AuditTraceDTO audit = new AuditTraceDTO();
            audit.setDateEvent(LocalDateTime.now());
            String keyValue = HelperAudit.getKeyStringValue(entity.getId());
            audit.setEntityName(entityName);
            audit.setEntityKeyValue(keyValue);
            audit.setEntityOldValue(oldValue);
            audit.setEntityNewValue(HelperString.toJson(entity));

            audits.add(audit);
        }
        return audits;
    }

}
