package it.andrea.start.controller.user;

import static it.andrea.start.constants.ApplicationConstants.DEFAULT_LANGUAGE;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
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

import it.andrea.start.annotation.Audit;
import it.andrea.start.annotation.CustomApiOperation;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.exception.user.UserAlreadyExistsException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.models.support.AuditActivity;
import it.andrea.start.models.support.AuditTypeOperation;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.service.user.UserService;
import it.andrea.start.utils.PageFilteringSortingUtility;
import it.andrea.start.utils.PagedResult;
import it.andrea.start.validator.OnCreate;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private UserService userService;
    
    public UserController(UserService userService) {
	super();
	this.userService = userService;
    }

    @CustomApiOperation(
	    method = "POST", 
	    description = "Aggiungi un utente da ADMIN o MANAGER", 
	    summary = "Aggiungi un utente da ADMIN o MANAGER")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.CREATE)
    public ResponseEntity<UserDTO> createUser(
	    HttpServletRequest httpServletRequest, 
	    @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, 
	    @RequestBody @Validated(OnCreate.class) UserDTO userDTO) throws BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

	userDTO = userService.createUser(userDTO, userDetails, language);

	return ResponseEntity.ok(userDTO);
    }

    @CustomApiOperation(
	    method = "PUT",
	    description = "Aggiorna un utente da ADMIN o MANAGER",
	    summary = "Aggiorna un utente da ADMIN o MANAGER"
	    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{id}")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.UPDATE)
    public ResponseEntity<UserDTO> updateUser(
	    HttpServletRequest httpServletRequest,
	    @RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language,
	    @RequestBody UserDTO userDTO, 
	    @PathVariable Long id) throws UserNotFoundException, BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {

	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

	userDTO.setId(id);
	userDTO = userService.updateUser(userDTO, userDetails, language).getLeft();
	
	return ResponseEntity.ok(userDTO);
    }

    @CustomApiOperation(
	    method = "DELETE",
	    description = "Elimina un utente da ADMIN o MANAGER",
	    summary = "Elimina un utente da ADMIN o MANAGER"
	    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.DELETE)
    public ResponseEntity<Void> deleteUser(
	    HttpServletRequest httpServletRequest, 
	    @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language, 
	    @PathVariable Long id) throws UserNotFoundException, BusinessException {

	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

	userService.deleteUser(id, userDetails, language);

	return ResponseEntity.ok().build();
    }

    @CustomApiOperation(
	    method = "GET",
	    description = "Informazioni di un utente",
	    summary = "Informazioni di un utente"
	    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> infoUser(
	    @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language, 
	    @PathVariable Long id) throws UserNotFoundException, MappingToDtoException  {

	UserDTO userDTO = userService.getUser(id);

	return ResponseEntity.ok(userDTO);
    }

    @CustomApiOperation(
	    method = "GET",
	    description = "Lista degli utenti",
	    summary = "Lista degli utenti"
	    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('OPERATOR')")
    @GetMapping("/list")
    public ResponseEntity<PagedResult<UserDTO>> listUser(
	    @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language, 
	    @RequestParam(required = false) Long id, 
	    @RequestParam(required = false) String username, 
	    @RequestParam(required = false) String textSearch, 
	    @RequestParam(required = false) UserStatus userStatus,
	    @RequestParam(required = false) String[] role,
	    @RequestParam(required = false) String[] sorts,
	    @RequestParam(required = false) Integer pageNum, 
	    @RequestParam(required = false) Integer pageSize) throws MappingToDtoException  {

	UserSearchCriteria criteria = new UserSearchCriteria();
	criteria.setId(id);
	criteria.setUsername(username);
	criteria.setTextSearch(textSearch);
	criteria.setUserStatus(userStatus);
	criteria.setRole(role);
	PageFilteringSortingUtility.applySort(criteria, sorts);

	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

	PagedResult<UserDTO> users;
	if (pageSize != null) {
	    users = userService.listUser(criteria, pageNum, pageSize, userDetails, language);
	} else {
	    users = userService.listUser(criteria, userDetails, language);
	}

	return ResponseEntity.ok(users);
    }

    @CustomApiOperation(
	    method = "PUT",
	    description = "Cambio password da ADMIN",
	    summary = "Cambio password da ADMIN"
	    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/change-password/{userId}")
    public ResponseEntity<Void> changePassword(
	    @RequestHeader(name = "accept-language", defaultValue = DEFAULT_LANGUAGE, required = false) String language, 
	    @PathVariable Long userId, 
	    @RequestBody ChangePassword changePassword) throws UserNotFoundException, BusinessException  {

	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	JWTokenUserDetails userDetails = (JWTokenUserDetails) authentication.getPrincipal();

	userService.changePassword(userId, changePassword.getNewPassword(), changePassword.getRepeatPassword(), userDetails, language);

	return ResponseEntity.ok().build();
    }

}
