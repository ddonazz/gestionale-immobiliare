package it.andrea.start.utils;

import java.util.Collection;

import it.andrea.start.constants.ApplicationConstants;
import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.exception.BusinessException;

public class HelperAuthorization {

	private HelperAuthorization() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	public static boolean hasRole(Collection<UserRoleDTO> roles, String role) {
		return roles.stream().anyMatch(userRole -> userRole.getRole().equals(role));
	}

	public static void checkSuperUserRoleAndEntityIsDeleted(Collection<UserRoleDTO> roles, boolean isDeleted, String messageException, String codeError, String... messageComponent) throws BusinessException {
		if (!hasRole(roles, ApplicationConstants.SYSTEM_ROLE_ADMIN) && !hasRole(roles, ApplicationConstants.SYSTEM_ROLE_MANAGER) && !hasRole(roles, ApplicationConstants.SYSTEM_ROLE_SUPERVISOR)) {
			return;
		}

		if (isDeleted) {
			if (messageComponent != null && messageComponent.length > 0) {
				throw new BusinessException(messageException, codeError, messageComponent);
			} else {
				throw new BusinessException(messageException, codeError);
			}
		}
	}
}
