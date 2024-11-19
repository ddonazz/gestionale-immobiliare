package it.andrea.start.service.user;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import it.andrea.start.dto.ListDTO;
import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.exception.user.UserAlreadyExistsException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.exception.user.UserRoleAlreadyExistsException;
import it.andrea.start.exception.user.UserRoleNotFoundException;
import it.andrea.start.searchcriteria.user.UserRoleSearchCriteria;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.utils.PagedResult;

public interface UserService {

	void requestPasswordReset(String email, String language) throws UserNotFoundException;

	UserDTO getUser(String username) throws UserNotFoundException, MappingToDtoException;

	UserDTO getUser(Long id) throws UserNotFoundException, MappingToDtoException;

	UserDTO getUserWho(JWTokenUserDetails jWTokenUserDetails) throws UserNotFoundException, MappingToDtoException;

	Pair<UserDTO, Collection<AuditTraceDTO>> createUser(UserDTO userDTO, JWTokenUserDetails userDetails, String language) throws BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException;

	Pair<UserDTO, Collection<AuditTraceDTO>> updateUser(UserDTO userDTO, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException;

	Collection<AuditTraceDTO> deleteUser(Long id, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException;

	PagedResult<UserDTO> listUser(UserSearchCriteria criteria, JWTokenUserDetails userDetails, String language) throws MappingToDtoException;

	PagedResult<UserDTO> listUser(UserSearchCriteria criteria, int pageNum, int pageSize, JWTokenUserDetails userDetails, String language) throws MappingToDtoException;

	void changePassword(Long userId, String newPassword, String repeatPassword, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException;

	void changePassword(String username, String newPassword, String repeatPassword, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException;

	/** ROLE USER */

	PagedResult<UserRoleDTO> listUserRole(UserRoleSearchCriteria criteria, int pageNum, int pageSize) throws BusinessException, MappingToDtoException;

	UserRoleDTO createUserRole(UserRoleDTO userRoleDTO) throws BusinessException, UserRoleAlreadyExistsException, MappingToEntityException, MappingToDtoException;

	UserRoleDTO updateUserRole(UserRoleDTO userRoleDTO) throws BusinessException, UserRoleNotFoundException, MappingToEntityException, MappingToDtoException;

	UserRoleDTO getUserRole(String roleName) throws BusinessException, UserRoleNotFoundException, MappingToDtoException;

	ListDTO listUser(JWTokenUserDetails userDetails, String language);

}
