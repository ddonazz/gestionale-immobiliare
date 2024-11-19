
package it.andrea.start.validator.user;

import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_REPEAT_PASSWORD_NOT_EQUAL;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_ADMIN_NOT_CHANGE_PASSWORD;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_ADMIN_NOT_USABLE;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_MANAGER_NOT_CHANGE_PASSWORD;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_MANAGER_NOT_USABLE;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.andrea.start.constants.ApplicationConstants;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.user.UserAlreadyExistsException;
import it.andrea.start.models.user.User;
import it.andrea.start.repository.user.UserRepository;

@Component
public class UserValidator {

	private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

	private static final String ENTITY = "User";

	private final UserRepository userRepository;

	public UserValidator(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	public void validateUser(UserDTO dto, boolean haveAdminRole, boolean checkAdmin) throws BusinessException, UserAlreadyExistsException {
		String username = dto.getUsername();
		if (userRepository.findByUsername(username).isPresent()) {
			throw new UserAlreadyExistsException(username);
		}

		String email = dto.getEmail();
		if (userRepository.findByEmail(email).isPresent()) {
			throw new UserAlreadyExistsException(username);
		}

		Collection<UserRoleDTO> roles = dto.getRoles();
		if (checkAdmin && roles.stream().anyMatch(role -> role.getRole().equals(ApplicationConstants.SYSTEM_ROLE_ADMIN))) {
			throw new BusinessException(ENTITY, "Role admin is not usable", CODE_USER_ROLE_ADMIN_NOT_USABLE);
		}

		UserRoleDTO roleManager = roles.stream().filter(role -> role.getRole().equals(ApplicationConstants.SYSTEM_ROLE_MANAGER)).findFirst().orElse(null);
		if (roleManager != null && !haveAdminRole) {
			throw new BusinessException(ENTITY, "Role manager is not usable", CODE_USER_ROLE_MANAGER_NOT_USABLE);
		}
	}

	public void validateUserUpdate(UserDTO dto, User entity, boolean haveAdminRole, boolean isMyProfile) throws BusinessException, UserAlreadyExistsException {
		String email = dto.getEmail();
		Optional<User> optionalUserOther = userRepository.findByEmail(email);
		if (optionalUserOther.isPresent() && !optionalUserOther.get().getId().equals(entity.getId())) {
			throw new UserAlreadyExistsException(email);
		}

		Collection<UserRoleDTO> roles = dto.getRoles();
		if (!isMyProfile && roles.stream().anyMatch(role -> role.getRole().equals(ApplicationConstants.SYSTEM_ROLE_ADMIN))) {
			throw new BusinessException(ENTITY, "Role admin is not usable", CODE_USER_ROLE_ADMIN_NOT_USABLE);
		}

		if (!isMyProfile) {
			UserRoleDTO roleManager = roles.stream().filter(role -> role.getRole().equals(ApplicationConstants.SYSTEM_ROLE_MANAGER)).findFirst().orElse(null);
			if (roleManager != null && !haveAdminRole) {
				throw new BusinessException(ENTITY, "Role manager is not usable", CODE_USER_ROLE_MANAGER_NOT_USABLE);
			}
		}

	}

	public static void checkPassword(User entity, String newPassword, String repeatPassword, boolean haveAdminRole, boolean haveManagerRole) throws BusinessException {
		if (haveAdminRole) {
			throw new BusinessException(ENTITY, "Is not possible to change password to admin role user", CODE_USER_ROLE_ADMIN_NOT_CHANGE_PASSWORD, "" + entity.getId());
		}
		if (haveManagerRole) {
			throw new BusinessException(ENTITY, "Is not possible to change password to manager role user if you not are admin user", CODE_USER_ROLE_MANAGER_NOT_CHANGE_PASSWORD, "" + entity.getId());
		}

		if (!newPassword.equals(repeatPassword)) {
			throw new BusinessException(ENTITY, "New password and repeat password is not equal", CODE_USER_REPEAT_PASSWORD_NOT_EQUAL);
		}
	}

}
