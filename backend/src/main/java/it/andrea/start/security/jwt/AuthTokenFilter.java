package it.andrea.start.security.jwt;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import it.andrea.start.configuration.context.tenant.TenantContextHolder;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
	this.jwtUtils = jwtUtils;
	this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
	try {
	    String jwt = parseJwt(wrappedRequest);
	    Optional<JWTokenUserDetails> jwtTokenUserDetailOpt = jwtUtils.validateAndParseToken(jwt);
	    if (jwtTokenUserDetailOpt.isPresent()) {
		String username = jwtTokenUserDetailOpt.get().getUsername();
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(wrappedRequest));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	    }
	} catch (Exception e) {
	    logger.error("Cannot set user authentication: {}", e);
	}
	
	try {
	    filterChain.doFilter(new ContentCachingRequestWrapper(wrappedRequest), response);	    
	} finally {
	    TenantContextHolder.resetTenant();
	}
	
    }

    private String parseJwt(HttpServletRequest request) {
	String headerAuth = request.getHeader("Authorization");
	if (headerAuth != null && headerAuth.startsWith(BEARER_PREFIX)) {
	    return headerAuth.substring(7);
	}
	return null;
    }
}