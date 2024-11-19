package it.andrea.start.controller.user;

import static it.andrea.start.constants.ApplicationConstants.DEFAULT_LANGUAGE;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.andrea.start.annotation.ApiRoleAccessNotes;
import it.andrea.start.annotation.ApiUserStatusAccessNotes;
import it.andrea.start.configuration.GlobalConfig;
import it.andrea.start.constants.ApplicationConstants;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.dto.ListDTO;
import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.exception.user.UserAlreadyExistsException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.models.support.AuditActivity;
import it.andrea.start.models.support.AuditTypeOperation;
import it.andrea.start.searchcriteria.user.UserRoleSearchCriteria;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.service.audit.AuditTraceServiceImpl;
import it.andrea.start.service.user.UserService;
import it.andrea.start.utils.HelperAudit;
import it.andrea.start.utils.PageFilteringSortingUtility;
import it.andrea.start.utils.PagedResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('" + ApplicationConstants.SYSTEM_ROLE_ADMIN_ANNOTATION + "')")
public class UserController {

    private GlobalConfig globalConfig;

    private UserService userService;
    private AuditTraceServiceImpl auditTraceService;

    public UserController(GlobalConfig globalConfig, UserService userService, AuditTraceServiceImpl auditTraceService) {
        super();
        this.globalConfig = globalConfig;
        this.userService = userService;
        this.auditTraceService = auditTraceService;
    }

    @Operation(
            method = "POST", 
            description = "Create user from admin or manager", 
            summary = "Create user from admin or manager", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
                    }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @ApiUserStatusAccessNotes()
    @PostMapping("/addUser")
    public ResponseEntity<UserDTO> createUser(HttpServletRequest httpServletRequest,
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, 
            @RequestBody(required = true) UserDTO userDTO) throws BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

        AuditTraceDTO auditAPICall = new AuditTraceDTO();

        try {

            auditAPICall = HelperAudit.getAuditControllerOperation(
                    globalConfig.getAuditLevel(),
                    AuditActivity.USER_OPERATION, 
                    userDetails,
                    AuditTypeOperation.CREATE, 
                    httpServletRequest, 
                    userDTO,
                    "UserController.createUser");

            Pair<UserDTO, Collection<AuditTraceDTO>> response = userService.createUser(userDTO, userDetails, language);

            userDTO = response.getLeft();
            Collection<AuditTraceDTO> audits = response.getRight();

            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, userDTO, null);
            HelperAudit.auditUnionsAndSettings(auditAPICall, audits);
            auditTraceService.saveAuditTrace(audits);

            return ResponseEntity.ok(userDTO);
        } catch (Exception ex) {

            auditAPICall.setActivity(AuditActivity.USER_OPERATION_EXCEPTION);
            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, null, ex);
            auditTraceService.saveAuditTrace(Stream.of(auditAPICall).toList());

            throw ex;
        }
    }

    @Operation(
            method = "PUT", 
            description = "Update user from admin or manager",
            summary = "Update user from admin or manager", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
                    }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @ApiUserStatusAccessNotes()
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<UserDTO> updateUser(HttpServletRequest httpServletRequest,
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @RequestBody(required = true) UserDTO userDTO,
            @PathVariable Long id) throws UserNotFoundException, BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException  {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

        AuditTraceDTO auditAPICall = new AuditTraceDTO();

        try {

            auditAPICall = HelperAudit.getAuditControllerOperation(
                    globalConfig.getAuditLevel(),
                    AuditActivity.USER_OPERATION,
                    userDetails,
                    AuditTypeOperation.UPDATE,
                    httpServletRequest, 
                    userDTO, 
                    "UserController.updateUser");

            userDTO.setId(id);
            Pair<UserDTO, Collection<AuditTraceDTO>> response = userService.updateUser(userDTO, userDetails, language);

            userDTO = response.getLeft();
            Collection<AuditTraceDTO> audits = response.getRight();

            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, userDTO, null);
            HelperAudit.auditUnionsAndSettings(auditAPICall, audits);
            auditTraceService.saveAuditTrace(audits);

            return ResponseEntity.ok(userDTO);
        } catch (Exception ex) {

            auditAPICall.setActivity(AuditActivity.USER_OPERATION_EXCEPTION);
            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, null, ex);
            auditTraceService.saveAuditTrace(Stream.of(auditAPICall).toList());

            throw ex;
        }
    }

    @Operation(
            method = "DELETE", 
            description = "Delete user from admin or manager", 
            summary = "Delete user from admin or manager", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
                    }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(HttpServletRequest httpServletRequest,
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language, 
            @PathVariable Long id) throws UserNotFoundException, BusinessException, MappingToEntityException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

        AuditTraceDTO auditAPICall = new AuditTraceDTO();

        try {

            auditAPICall = HelperAudit.getAuditControllerOperation(
                    globalConfig.getAuditLevel(), 
                    AuditActivity.USER_OPERATION, 
                    userDetails,
                    AuditTypeOperation.DELETE, 
                    httpServletRequest, 
                    null, 
                    "UserController.deleteUser");

            Collection<AuditTraceDTO> audits = userService.deleteUser(id, userDetails, language);

            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, null, null);
            HelperAudit.auditUnionsAndSettings(auditAPICall, audits);
            auditTraceService.saveAuditTrace(audits);

            return ResponseEntity.ok().build();
        } catch (Exception ex) {

            auditAPICall.setActivity(AuditActivity.USER_OPERATION_EXCEPTION);
            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, null, ex);
            auditTraceService.saveAuditTrace(Stream.of(auditAPICall).toList());

            throw ex;
        }
    }

    @Operation(
            method = "GET",
            description = "Info about user",
            summary = "Info about user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
            }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @GetMapping("/infoUser/{id}") 
    public ResponseEntity<UserDTO> infoUser(
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language,
            @PathVariable Long id) throws Exception {

        UserDTO userDTO = userService.getUser(id);

        return ResponseEntity.ok(userDTO);
    }

    @Operation(
            method = "GET",
            description = "List user",
            summary = "List user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                    }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION, ApplicationConstants.SYSTEM_ROLE_SUPERVISOR_ANNOTATION, ApplicationConstants.SYSTEM_ROLE_OPERATOR_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER, ApplicationConstants.SYSTEM_ROLE_SUPERVISOR, ApplicationConstants.SYSTEM_ROLE_OPERATOR })
    @ApiUserStatusAccessNotes
    @GetMapping("/listUser") 
    public ResponseEntity<PagedResult<UserDTO>> listUser(
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) UserStatus userStatus,
            @RequestParam(required = false) String[] role,
            @RequestParam(required = false) String[] sorts,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws Exception {

        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setId(id);
        criteria.setUsername(username);
        criteria.setTextSearch(textSearch);
        criteria.setUserStatus(userStatus);
        criteria.setRole(role);
        PageFilteringSortingUtility.applySort(criteria, sorts);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails)authentication.getPrincipal();

        PagedResult<UserDTO> users;
        if(pageSize != null) {
            users = userService.listUser(criteria, pageNum, pageSize, userDetails, language);            
        } else {
            users = userService.listUser(criteria, userDetails, language);            
        }

        return ResponseEntity.ok(users);
    }


    @Operation(
            method = "GET",
            description = "List role user",
            summary = "List role user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @GetMapping("/listUserRole") 
    public ResponseEntity<PagedResult<UserRoleDTO>> listUserRole(
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String[] sorts,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws Exception {

        UserRoleSearchCriteria criteria = new UserRoleSearchCriteria();
        criteria.setRoleName(role);
        PageFilteringSortingUtility.applySort(criteria, sorts);

        PagedResult<UserRoleDTO> users = userService.listUserRole(criteria, pageNum, pageSize);

        return ResponseEntity.ok(users);
    }


    @Operation(
            method = "GET",
            description = "Information of role user",
            summary = "Information of role user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @GetMapping("/role/{roleName}") 
    public ResponseEntity<UserRoleDTO> getUserRole(
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language,
            @PathVariable(value = "roleName") String roloName) throws Exception {

        UserRoleDTO userRoleDTO = userService.getUserRole(roloName);
        return ResponseEntity.ok(userRoleDTO);
    }


    @Operation(
            method = "POST",
            description = "Create new role user",
            summary = "Create new role user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                }
            )
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN })
    @PostMapping("/role") 
    public ResponseEntity<UserRoleDTO> createUserRole(
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @RequestBody(required = true) UserRoleDTO userRoleDTO) throws Exception {

        userRoleDTO = userService.createUserRole(userRoleDTO);
        return ResponseEntity.ok(userRoleDTO);
    }

    @Operation(
            method = "PUT",
            description = "Update role user",
            summary = "Update role user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                }
            )
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN })
    @PutMapping("/role") 
    public ResponseEntity<UserRoleDTO> updateUserRole(
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language,
            @RequestBody(required = true) UserRoleDTO userRoleDTO) throws Exception {

        userRoleDTO = userService.updateUserRole(userRoleDTO);
        
        return ResponseEntity.ok(userRoleDTO);
    }


    @Operation(
            method = "GET",
            description = "List user for combo",
            summary = "List user for combo",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION, ApplicationConstants.SYSTEM_ROLE_OPERATOR_ANNOTATION })
    @ApiRoleAccessNotes(roles = { ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER })
    @GetMapping("/listUserForCombo")
    public ResponseEntity<ListDTO> listUserForCombo(@RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails)authentication.getPrincipal();

        ListDTO listUser = userService.listUser(userDetails, language);

        return ResponseEntity.ok(listUser);
    }


    @Operation(
            method = "PUT",
            description = "User change password only from admin user",
            summary = "User change password only from admin user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
                }
            )
    @Secured({ ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION})
    @ApiRoleAccessNotes(roles = {ApplicationConstants.SYSTEM_ROLE_ADMIN, ApplicationConstants.SYSTEM_ROLE_MANAGER})
    @PutMapping("/changePassword/{userId}")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language,
            @PathVariable Long userId,
            @RequestBody(required = true) ChangePassword changePassword) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails)authentication.getPrincipal();
        
        userService.changePassword(userId, changePassword.getNewPassword(), changePassword.getRepeatPassword(), userDetails, language);

        return ResponseEntity.ok().build();
    }


}
