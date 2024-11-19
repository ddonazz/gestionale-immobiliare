package it.andrea.start.mappers.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.mappers.AbstractMapper;
import it.andrea.start.models.user.User;
import it.andrea.start.models.user.UserRole;
import jakarta.persistence.EntityManager;

@Service
public class UserMapper extends AbstractMapper<UserDTO, User> {

	private UserRoleMapper userRoleMapper;

	public UserMapper(EntityManager entityManager, UserRoleMapper userRoleMapper) {
		super(entityManager);
		this.userRoleMapper = userRoleMapper;
	}

    @Override
    public UserDTO toDto(User entity) throws MappingToDtoException {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setUserStatus(entity.getUserStatus());
        dto.setRoles(userRoleMapper.toDtos(entity.getRoles()));
        dto.setLanguageDefault(entity.getLanguageDefault());

        return dto;
    }

    @Override
    public void toEntity(UserDTO dto, User entity) throws MappingToEntityException {
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setUserStatus(dto.getUserStatus());
        entity.setLanguageDefault(dto.getLanguageDefault());

        Collection<UserRoleDTO> userRolesDto = dto.getRoles();
        List<UserRole> userRoles = new ArrayList<>();
        if (userRolesDto != null && !userRolesDto.isEmpty()) {
            for (UserRoleDTO userRoleDto : userRolesDto) {
                UserRole userRole = getEntityManager().find(UserRole.class, userRoleDto.getRole());
                if (userRole == null) {
                    throw new MappingToEntityException();
                }
                userRoles.add(userRole);
            }
        }
        entity.setRoles(userRoles);
    }

}
