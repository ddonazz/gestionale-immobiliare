package it.andrea.start.security.service;

import static it.andrea.start.constants.ApplicationConstants.DEFAULT_LANGUAGE;
import static it.andrea.start.constants.ApplicationConstants.TOKEN_VALIDITY_DAYS;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.andrea.start.dto.user.UserRoleDTO;
import it.andrea.start.models.user.User;
import it.andrea.start.repository.user.UserRepository;
import it.andrea.start.support.UserAccountData;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Collection<UserRoleDTO> rolesDTO = user.getRoles().stream().map(role -> new UserRoleDTO(role.getRole(), role.getRoleDescription())).collect(Collectors.toList());

        UserAccountData userAccountData = new UserAccountData();
        userAccountData.setEmail(user.getEmail());
        userAccountData.setRoles(rolesDTO);

        JWTokenUserDetails jWTokenUserDetails = JWTokenUserDetails.build(user, userAccountData);
        LocalDateTime now = LocalDateTime.now();
        jWTokenUserDetails.setIssuedAt(now);
        jWTokenUserDetails.setExpiration(now.plus(Duration.ofDays(TOKEN_VALIDITY_DAYS)));

        return jWTokenUserDetails;
    }

    public UserAccountData reloadUserData(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not Found with username: " + username));

        Collection<UserRoleDTO> rolesDTO = user.getRoles().stream().map(role -> new UserRoleDTO(role.getRole(), role.getRoleDescription())).toList();

        UserAccountData userAccountData = new UserAccountData();
        userAccountData.setEmail(user.getEmail());
        userAccountData.setRoles(rolesDTO);

        String languageDefault = StringUtils.hasText(user.getLanguageDefault()) ? user.getLanguageDefault() : DEFAULT_LANGUAGE;
        userAccountData.setLanguageDefault(languageDefault);

        return userAccountData;
    }

}
