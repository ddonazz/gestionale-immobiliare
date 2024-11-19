package it.andrea.start.controller.user;

import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.andrea.start.configuration.GlobalConfig;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.controller.response.TokenResponse;
import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.controller.types.RecoveryPassword;
import it.andrea.start.controller.types.ResetPasswordContent;
import it.andrea.start.controller.types.UserAndPassword;
import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.ExceptionCodeError;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.models.support.AuditActivity;
import it.andrea.start.models.support.AuditTypeOperation;
import it.andrea.start.security.jwt.JwtUtils;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.service.audit.AuditTraceServiceImpl;
import it.andrea.start.service.user.UserService;
import it.andrea.start.utils.HelperAudit;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/authorize")
public class AuthorizeController {

    private GlobalConfig globalConfig;

    private UserService userService;
    private AuditTraceServiceImpl auditTraceService;

    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    public AuthorizeController(GlobalConfig globalConfig, UserService userService, AuditTraceServiceImpl auditTraceService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        super();
        this.globalConfig = globalConfig;
        this.userService = userService;
        this.auditTraceService = auditTraceService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Operation(
            method = "POST", 
            description = "Login user",
            summary = "Login user", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credentials is valid"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Credentials not valid"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
            }
            )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authorize(HttpServletRequest httpServletRequest,
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @RequestBody @Validated UserAndPassword userAndPassword) throws BusinessException, MappingToEntityException {

        AuditTraceDTO auditAPICall = new AuditTraceDTO();

        try {
            auditAPICall = HelperAudit.getAuditControllerOperation(
                    globalConfig.getAuditLevel(), 
                    AuditActivity.ANONYMOUS, 
                    null,
                    AuditTypeOperation.LOGIN,
                    httpServletRequest, 
                    userAndPassword, 
                    "AuthorizeController.authorize");

            Authentication authentication = authenticate(userAndPassword.getUsername(), userAndPassword.getPassword());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            TokenResponse tokenResponse = new TokenResponse(jwt);

            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, tokenResponse, null);
            auditTraceService.saveAuditTrace(Stream.of(auditAPICall).toList());

            return ResponseEntity.ok(tokenResponse);
        }
        catch (Exception ex) {
            auditAPICall.setActivity(AuditActivity.ANONYMOUS_EXCEPTION);
            HelperAudit.auditControllerOperationAddResponseAndException(auditAPICall, null, ex);
            auditTraceService.saveAuditTrace(Stream.of(auditAPICall).toList());

            throw ex;
        }
    }

    @Operation(
            method = "POST", 
            description = "Recovery password sending email", 
            summary = "Recovery password sending email", responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
            }
            )
    @PostMapping("/recoveryPassword")
    public ResponseEntity<Void> recoveryPassword(
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @RequestBody RecoveryPassword recoveryPassword) throws Exception {

        return ResponseEntity.ok().build();
    }

    @Operation(
            method = "GET",
            description = "Validation token reset password user", 
            summary = "Validation token reset password user", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"), 
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
            }
            )
    @GetMapping("/validitationTokenResetPasword/{token}")
    public ResponseEntity<Void> validitationTokenResetPasword(
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @PathVariable String token) throws Exception {

        return ResponseEntity.ok().build();
    }

    @Operation(
            method = "POST", 
            description = "User reset password", 
            summary = "User reset password", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"), 
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
            }
            )
    @PostMapping("/resetPassword")
    public ResponseEntity<Void> resetPassword(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @RequestBody(required = true) ResetPasswordContent resetPasswordContent) throws Exception {

        return ResponseEntity.ok().build();
    }

    @Operation(
            method = "GET", 
            description = "Information current user", 
            summary = "Information current user", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true)))
            }
            )
    @GetMapping("/whoami")
    public ResponseEntity<UserDTO> whoami(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

        UserDTO dto = userService.getUserWho(userDetails);

        return ResponseEntity.ok(dto);
    }

    @Operation(
            method = "PUT", 
            description = "Update information current user", 
            summary = "Update information current user", 
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"), 
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
            }
            )
    @PutMapping("/updateProfile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @RequestBody(required = true) UserDTO userDTO) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

        userService.updateUser(userDTO, userDetails, language);

        return ResponseEntity.ok(userDTO);
    }

    @Operation(
            method = "POST", 
            description = "User self change password", 
            summary = "User self change password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"), 
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) 
            }
            )
    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(
    		@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
            @RequestBody(required = true) ChangePassword changePassword) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        userService.changePassword(username, changePassword.getNewPassword(), changePassword.getRepeatPassword(), null, language);

        return ResponseEntity.ok().build();
    }

    private Authentication authenticate(String username, String password) throws BusinessException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        JWTokenUserDetails principal = (JWTokenUserDetails) authentication.getPrincipal();
        UserStatus status = principal.getUserStatus();
        if (status == UserStatus.PENDING) {
            throw new BusinessException("Your account is pending", ExceptionCodeError.CODE_USER_AUTHORIZE_LOGIN_ACCOUNT_PENDING);
        } 
        else if (status == UserStatus.SUSPENDED) {
            throw new BusinessException("Your account is suspended", ExceptionCodeError.CODE_USER_AUTHORIZE_LOGIN_ACCOUNT_SUSPENDED);
        } 
        else if (status == UserStatus.DEACTIVE) {
            throw new BusinessException("Your account is deactivated", ExceptionCodeError.CODE_USER_AUTHORIZE_LOGIN_ACCOUNT_DEACTIVE);
        } 
        else if (status == UserStatus.BLACKLIST) {
            throw new BusinessException("Your account is in blacklist", ExceptionCodeError.CODE_USER_AUTHORIZE_LOGIN_ACCOUNT_BLACKLIST);
        }

        return authentication;
    }

}
