package it.andrea.start.service.user;

import static it.andrea.start.constants.ApplicationConstants.MAX_PAGE_SIZE;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ENTITY_MANAGE;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ROLE_ADMIN;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ROLE_ADMIN_ANNOTATION;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ROLE_MANAGER;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ROLE_OPERATOR_ANNOTATION;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_ADMIN_NOT_DELETE;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_MANAGER_NOT_DELETE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.andrea.start.configuration.GlobalConfig;
import it.andrea.start.constants.AuditLevel;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.dto.ListDTO;
import it.andrea.start.dto.ListItemDTO;
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
import it.andrea.start.mappers.user.UserMapper;
import it.andrea.start.mappers.user.UserRoleMapper;
import it.andrea.start.models.user.User;
import it.andrea.start.models.user.UserRole;
import it.andrea.start.repository.user.UserRepository;
import it.andrea.start.repository.user.UserRoleRepository;
import it.andrea.start.searchcriteria.specification.ElementSearchCriteria;
import it.andrea.start.searchcriteria.user.UserRoleSearchCriteria;
import it.andrea.start.searchcriteria.user.UserRoleSearchSpecification;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.searchcriteria.user.UserSearchSpecification;
import it.andrea.start.security.IEncrypterManager;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.utils.HelperAudit;
import it.andrea.start.utils.HelperAuthorization;
import it.andrea.start.utils.HelperString;
import it.andrea.start.utils.PageFilteringSortingUtility;
import it.andrea.start.utils.PagedResult;
import it.andrea.start.validator.user.UserValidator;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	private static final String ENTITY = "User";

	private final GlobalConfig globalConfig;
	private final IEncrypterManager encrypterManager;

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;

	private final UserMapper userMapper;
	private final UserRoleMapper userRoleMapper;

	private final UserValidator userValidator;

	public UserServiceImpl(GlobalConfig globalConfig, IEncrypterManager encrypterManager, UserRepository userRepository, UserRoleRepository userRoleRepository, UserMapper userMapper, UserRoleMapper userRoleMapper, UserValidator userValidator) {
		super();
		this.globalConfig = globalConfig;
		this.encrypterManager = encrypterManager;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.userMapper = userMapper;
		this.userRoleMapper = userRoleMapper;
		this.userValidator = userValidator;
	}

	@Override
	public void requestPasswordReset(String email, String language) throws UserNotFoundException {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(email);
		}
	}

	@Override
	@Transactional(noRollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
	public UserDTO getUser(String username) throws UserNotFoundException, MappingToDtoException {
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(username);
		}
		User user = optionalUser.get();

		return userMapper.toDto(user);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public UserDTO getUser(Long id) throws UserNotFoundException, MappingToDtoException {
		Optional<User> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(id.toString());
		}
		User user = optionalUser.get();

		return userMapper.toDto(user);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public UserDTO getUserWho(JWTokenUserDetails jWTokenUserDetails) throws UserNotFoundException, MappingToDtoException {
		String username = jWTokenUserDetails.getUsername();
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(username);
		}
		User user = optionalUser.get();

		return userMapper.toDto(user);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public Pair<UserDTO, Collection<AuditTraceDTO>> createUser(UserDTO userDTO, JWTokenUserDetails userDetails, String language) throws BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {
		Collection<AuditTraceDTO> audits = new ArrayList<>();

		userValidator.validateUser(userDTO, HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN_ANNOTATION), true);

		User user = new User();
		userMapper.toEntity(userDTO, user);
		String passwordCrypt = encrypterManager.encode(userDTO.getPassword());
		user.setPassword(passwordCrypt);

		String creator = userDetails.getUsername();
		if (StringUtils.isBlank(creator)) {
			creator = SYSTEM_ENTITY_MANAGE;
		}

		user.setCreator(creator);
		user.setLastModifiedBy(creator);

		userRepository.save(user);

		HelperAudit.generateAudit(audits, globalConfig, user, ENTITY, null);

		userDTO = userMapper.toDto(user);
		return Pair.of(userDTO, audits);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public Pair<UserDTO, Collection<AuditTraceDTO>> updateUser(UserDTO userDTO, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {
		Collection<AuditTraceDTO> audits = new ArrayList<>();
		String oldValue = null;

		boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN_ANNOTATION);

		String username = userDTO.getUsername();
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(username);
		}
		User user = optionalUser.get();

		if (globalConfig.getAuditLevel() == AuditLevel.ALL || globalConfig.getAuditLevel() == AuditLevel.DATABASE) {
			oldValue = HelperString.toJson(user);
		}

		boolean isMyProfile = false;
		if (user.getId().compareTo(userDetails.getId()) == 0) {
			isMyProfile = true;
		}

		userValidator.validateUserUpdate(userDTO, user, haveAdminRole, isMyProfile);

		userMapper.toEntity(userDTO, user);
		userRepository.save(user);

		HelperAudit.generateAudit(audits, globalConfig, user, ENTITY, oldValue);

		userDTO = this.userMapper.toDto(user);
		return Pair.of(userDTO, audits);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public Collection<AuditTraceDTO> deleteUser(Long id, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException {
		Collection<AuditTraceDTO> audits = new ArrayList<>();

		boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN);
		boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_MANAGER);

		Optional<User> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(id.toString());
		}
		User user = optionalUser.get();

		if (haveAdminRole) {
			throw new BusinessException(ENTITY, "Admin not possible to delete", CODE_USER_ROLE_ADMIN_NOT_DELETE, String.valueOf(id));
		}
		if (haveManagerRole) {
			throw new BusinessException(ENTITY, "Only admin can delete a manager", CODE_USER_ROLE_MANAGER_NOT_DELETE, String.valueOf(id));
		}

		user.setUserStatus(UserStatus.DEACTIVE);
		user.setLastModifiedBy(userDetails.getUsername());

		userRepository.save(user);

		HelperAudit.generateAudit(audits, globalConfig, user, ENTITY, null);

		return audits;
	}

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
	public PagedResult<UserDTO> listUser(UserSearchCriteria criteria, JWTokenUserDetails userDetails, String language) throws MappingToDtoException {
		return this.listUser(criteria, 0, -1, userDetails, language);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
	public PagedResult<UserDTO> listUser(UserSearchCriteria criteria, int pageNum, int pageSize, JWTokenUserDetails userDetails, String language) throws MappingToDtoException {

		boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN_ANNOTATION);
		boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_MANAGER_ANNOTATION);
		boolean haveOperatorRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_OPERATOR_ANNOTATION);

		List<Sort> sortList = PageFilteringSortingUtility.generateSortList(criteria.getSort());
		Sort sort = PageFilteringSortingUtility.getSortSequence(sortList).orElse(Sort.by(Direction.ASC, "username"));
		if (pageSize == -1) {
			pageNum = 1;
			pageSize = MAX_PAGE_SIZE;
		}

		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

		String[] roles = criteria.getRole();
		List<String> rolesNotUsable = new ArrayList<>();

		if (haveAdminRole) {
			// admin può vedere tutti gli utenti
		} else if (haveManagerRole) {
			if (roles != null) {
				roles = Arrays.stream(roles).filter(role -> !role.equals(SYSTEM_ROLE_ADMIN)).toArray(String[]::new);
			}
			rolesNotUsable.add(SYSTEM_ROLE_ADMIN);
			criteria.setRoleNotValid(rolesNotUsable.toArray(new String[0]));
		} else if (haveOperatorRole) {
			roles = Arrays.stream(roles).filter(role -> !role.equals(SYSTEM_ROLE_ADMIN) && !role.equals(SYSTEM_ROLE_MANAGER)).toArray(String[]::new);
			rolesNotUsable.add(SYSTEM_ROLE_ADMIN);
			rolesNotUsable.add(SYSTEM_ROLE_MANAGER);
			criteria.setRoleNotValid(rolesNotUsable.toArray(new String[0]));
		} else {
			// nessun ruolo valido, probabilmente gestire questa situazione
		}

		Page<User> page = userRepository.findAll(new UserSearchSpecification(criteria), pageable);

		PagedResult<UserDTO> result = new PagedResult<>();
		PageFilteringSortingUtility.computePage(result, (int) page.getTotalElements(), pageNum, pageSize);

		List<User> users = page.getContent();
		result.setItems(userMapper.toDtos(users));

		return result;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
	public void changePassword(Long userId, String newPassword, String repeatPassword, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException {
		boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN_ANNOTATION);
		boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_MANAGER_ANNOTATION);

		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(userId.toString());
		}
		User user = optionalUser.get();

		UserValidator.checkPassword(user, newPassword, repeatPassword, haveAdminRole, haveManagerRole);

		String passwordCrypt = encrypterManager.encode(newPassword);
		user.setPassword(passwordCrypt);
		user.setLastModifiedBy(userDetails.getUsername());

		userRepository.save(user);
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
	public void changePassword(String username, String newPassword, String repeatPassword, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException {
		boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN_ANNOTATION);
		boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_MANAGER_ANNOTATION);

		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isEmpty()) {
			throw new UserNotFoundException(username);
		}
		User user = optionalUser.get();

		UserValidator.checkPassword(user, newPassword, repeatPassword, haveAdminRole, haveManagerRole);

		String passwordCrypt = encrypterManager.encode(newPassword);
		user.setPassword(passwordCrypt);
		user.setLastModifiedBy(userDetails.getUsername());

		userRepository.save(user);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public PagedResult<UserRoleDTO> listUserRole(UserRoleSearchCriteria criteria, int pageNum, int pageSize) throws MappingToDtoException {
		List<ElementSearchCriteria> searchCriteria = criteria.getSearchCriteria();
		UserRoleSearchSpecification specList = new UserRoleSearchSpecification(searchCriteria);
		List<Sort> sortList = PageFilteringSortingUtility.generateSortList(criteria.getSort());
		Sort sort = PageFilteringSortingUtility.getSortSequence(sortList).orElse(Sort.by(Direction.ASC, "role"));

		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

		Page<UserRole> page = userRoleRepository.findAll(specList, pageable);

		PagedResult<UserRoleDTO> result = new PagedResult<>();

		int numRoles = (int) page.getTotalElements();
		PageFilteringSortingUtility.computePage(result, numRoles, pageNum, pageSize);

		List<UserRole> roles = page.getContent();
		Collection<UserRoleDTO> items = new ArrayList<>();
		for (UserRole userRole : roles) {
			UserRoleDTO item = userRoleMapper.toDto(userRole);
			items.add(item);
		}
		result.setItems(items);

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public UserRoleDTO createUserRole(UserRoleDTO userRoleDTO) throws UserRoleAlreadyExistsException, BusinessException, MappingToEntityException, MappingToDtoException {

		String role = userRoleDTO.getRole();

		if (userRoleRepository.findByRole(role).isPresent()) {
			throw new UserRoleAlreadyExistsException(role);
		}

		UserRole userRole = new UserRole();
		userRoleMapper.toEntity(userRoleDTO, userRole);

		userRoleRepository.save(userRole);

		return userRoleMapper.toDto(userRole);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public UserRoleDTO updateUserRole(UserRoleDTO userRoleDTO) throws BusinessException, UserRoleNotFoundException, MappingToEntityException, MappingToDtoException {

		String role = userRoleDTO.getRole();

		Optional<UserRole> userRoleOpt = userRoleRepository.findByRole(role);
		if (!userRoleOpt.isPresent()) {
			throw new UserRoleNotFoundException(role);
		}
		UserRole userRole = userRoleOpt.get();

		userRoleMapper.toEntity(userRoleDTO, userRole);

		userRoleRepository.save(userRole);

		return userRoleMapper.toDto(userRole);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
	public UserRoleDTO getUserRole(String roleName) throws BusinessException, UserRoleNotFoundException, MappingToDtoException {
		Optional<UserRole> userRoleOpt = userRoleRepository.findByRole(roleName);
		if (!userRoleOpt.isPresent()) {
			throw new UserRoleNotFoundException(roleName);
		}
		UserRole userRole = userRoleOpt.get();

		return userRoleMapper.toDto(userRole);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
	public ListDTO listUser(JWTokenUserDetails userDetails, String language) {
		boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_ADMIN_ANNOTATION);
		boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getRoles(), SYSTEM_ROLE_MANAGER_ANNOTATION);

		List<User> usersFind = null;
		if (haveAdminRole) {
			usersFind = userRepository.findAll();
		} else if (haveManagerRole) {
			usersFind = userRepository.findAllNotAdmin();
		}

		ListDTO response = new ListDTO();
		Collection<ListItemDTO> items = new ArrayList<>();
		response.setItems(items);
		if (usersFind != null) {
			for (User userItem : usersFind) {
				ListItemDTO item = new ListItemDTO();
				item.setId(userItem.getId().toString());
				item.setDescription(userItem.getUsername());
				items.add(item);
			}
		}

		return response;
	}

}
