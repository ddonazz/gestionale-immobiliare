package it.andrea.start.service.user;

import static it.andrea.start.constants.ApplicationConstants.MAX_PAGE_SIZE;
import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ENTITY_MANAGE;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_ADMIN_NOT_DELETE;
import static it.andrea.start.exception.ExceptionCodeError.CODE_USER_ROLE_MANAGER_NOT_DELETE;

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
import it.andrea.start.constants.RoleType;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.exception.user.UserAlreadyExistsException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.mappers.user.UserMapper;
import it.andrea.start.models.user.User;
import it.andrea.start.repository.user.UserRepository;
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

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    public UserServiceImpl(GlobalConfig globalConfig, IEncrypterManager encrypterManager, UserRepository userRepository, UserMapper userMapper, UserValidator userValidator) {
	super();
	this.globalConfig = globalConfig;
	this.encrypterManager = encrypterManager;
	this.userRepository = userRepository;
	this.userMapper = userMapper;
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
    public UserDTO createUser(UserDTO userDTO, JWTokenUserDetails userDetails, String language) throws BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {
	userValidator.validateUser(userDTO, HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN), true);

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

	return userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Pair<UserDTO, AuditTraceDTO> updateUser(UserDTO userDTO, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException, MappingToEntityException, MappingToDtoException, UserAlreadyExistsException {
	boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN);

	String username = userDTO.getUsername();
	Optional<User> optionalUser = userRepository.findByUsername(username);
	if (optionalUser.isEmpty()) {
	    throw new UserNotFoundException(username);
	}
	User user = optionalUser.get();

	AuditTraceDTO audit = new AuditTraceDTO();
	String oldValue = null;
	if (globalConfig.getAuditLevel() == AuditLevel.ALL || globalConfig.getAuditLevel() == AuditLevel.DATABASE) {
	    oldValue = HelperString.toJson(user);
	}

	boolean isMyProfile = false;
	if (user.getUsername().compareTo(userDetails.getUsername()) == 0) {
	    isMyProfile = true;
	}

	userValidator.validateUserUpdate(userDTO, user, haveAdminRole, isMyProfile);

	userMapper.toEntity(userDTO, user);
	userRepository.save(user);

	HelperAudit.generateAudit(audit, globalConfig, user, ENTITY, oldValue);

	userDTO = this.userMapper.toDto(user);
	return Pair.of(userDTO, audit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteUser(Long id, JWTokenUserDetails userDetails, String language) throws UserNotFoundException, BusinessException {
	boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN);
	boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_MANAGER);

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
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
    public PagedResult<UserDTO> listUser(UserSearchCriteria criteria, JWTokenUserDetails userDetails, String language) throws MappingToDtoException {
	return this.listUser(criteria, 0, -1, userDetails, language);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRED)
    public PagedResult<UserDTO> listUser(UserSearchCriteria criteria, int pageNum, int pageSize, JWTokenUserDetails userDetails, String language) throws MappingToDtoException {
	List<Sort> sortList = PageFilteringSortingUtility.generateSortList(criteria.getSort());
	Sort sort = PageFilteringSortingUtility.getSortSequence(sortList).orElse(Sort.by(Direction.ASC, "username"));
	if (pageSize == -1) {
	    pageNum = 1;
	    pageSize = MAX_PAGE_SIZE;
	}

	Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

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
	boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN);
	boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_MANAGER);

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
	boolean haveAdminRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN);
	boolean haveManagerRole = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_MANAGER);

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

}
