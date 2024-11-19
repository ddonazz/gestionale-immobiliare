package it.andrea.start.security.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.security.crypto.CryptoUtils;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.support.UserAccountData;
import it.andrea.start.utils.HelperDate;
import it.andrea.start.utils.HelperString;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JwtUtils {

	private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);

	private static final String AUTHORIZATION_TOKEN_VALUE_PREFIX = "Bearer ";
	private static final String CLAIM_USERNAME = "username";
	private static final String CLAIM_NAME = "name";
	private static final String CLAIM_ACCOUNT_DATA = "accountData";
	private static final String CLAIM_USER_STATUS = "userStatus";

	private final String secretKey;
	private final String pathKey;

	private final CryptoUtils cryptoUtils;

	public JwtUtils(Environment environment) {
		this.secretKey = environment.getProperty("jwt.secret");
		this.pathKey = environment.getProperty("app.key.offuscator.path");
		this.cryptoUtils = initializeCrypto();
	}

	private CryptoUtils initializeCrypto() {
		try {
			CryptoUtils cryptoUtils = CryptoUtils.getInstance();
			cryptoUtils.generateAES256Key(pathKey);
			return cryptoUtils;
		} catch (NoSuchAlgorithmException | IOException e) {
			LOG.error("Error initializing crypto: {}", e.getMessage());
			throw new RuntimeException("Crypto initialization failed", e);
		}
	}

	public String generateJwtToken(Authentication authentication) {
		JWTokenUserDetails userPrincipal = (JWTokenUserDetails) authentication.getPrincipal();
		String encodedData = offuscateUserAccountData(userPrincipal.getUserAccountData());

		JwtBuilder builder = Jwts.builder()
				.issuedAt(HelperDate.localDateTimeToDate(userPrincipal.getIssuedAt()))
				.expiration(HelperDate.localDateTimeToDate(userPrincipal.getExpiration()))
				.subject(userPrincipal.getId().toString())
				.claim(CLAIM_USERNAME, userPrincipal.getUsername())
				.claim(CLAIM_NAME, userPrincipal.getName())
				.claim(CLAIM_ACCOUNT_DATA, encodedData)
				.claim(CLAIM_USER_STATUS, userPrincipal.getUserStatus().name())
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes()));

		return AUTHORIZATION_TOKEN_VALUE_PREFIX + builder.compact();
	}

	public Optional<JWTokenUserDetails> decodeJwtToken(String token) {
		Claims claims = extractAllClaims(token);

		LocalDateTime issuedAt = HelperDate.dateToLocalDateTime(claims.getIssuedAt());
		Long userId = Long.valueOf(claims.getSubject());
		LocalDateTime expiration = HelperDate.dateToLocalDateTime(claims.getExpiration());
		String username = claims.get(CLAIM_USERNAME).toString();
		String name = claims.get(CLAIM_NAME).toString();
		Optional<UserAccountData>userAccountDataOpt = deOffuscateUserAccountData(claims.get(CLAIM_ACCOUNT_DATA).toString());
		UserStatus userStatus = UserStatus.valueOf(claims.get(CLAIM_USER_STATUS).toString());

		if(userAccountDataOpt.isPresent()) {
			List<GrantedAuthority> authorities = userAccountDataOpt.get().getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole())).collect(Collectors.toList());
			JWTokenUserDetails userDetails = new JWTokenUserDetails(userId, username, username, name, userAccountDataOpt.get().getEmail(), userStatus, userAccountDataOpt.get().getRoles(), authorities, userAccountDataOpt.get());
			userDetails.setIssuedAt(issuedAt);
			userDetails.setExpiration(expiration);
			
			return Optional.of(userDetails);
		}
		
		return Optional.empty();
	}

	public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

	public boolean validateJwtToken(String authToken) {
		JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build();
		try {
			jwtParser.parse(authToken);
		} catch (Exception e) {
			LOG.error("Invalid JWT token: {}", e.getMessage());
			return false;
		}
		
		return true;
	}

	private Claims extractAllClaims(String token) {
		JwtParserBuilder parserBuilder = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()));
		return parserBuilder.build().parseSignedClaims(token).getPayload();
	}

	private String offuscateUserAccountData(UserAccountData userAccountData) {
		if (userAccountData == null) {
			return null;
		}
		String json = HelperString.toJson(userAccountData);
		try {
			return cryptoUtils.encryptAES256String(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			LOG.error("Error encrypting user account data: {}", e.getMessage());
			throw new RuntimeException("User account data encryption failed", e);
		}
	}

	private Optional<UserAccountData> deOffuscateUserAccountData(String encryptedString) {
		if (encryptedString == null) {
			return Optional.empty();
		}
			String decryptedString = "";
			try {
				decryptedString = cryptoUtils.decryptAES256String(encryptedString);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
				LOG.error("Error decrypting user account data: {}", e.getMessage());
				throw new RuntimeException("User account data decryption failed", e);
			}
			byte[] decodedBytes = Base64.getDecoder().decode(decryptedString);
			UserAccountData decryptedInfo = HelperString.fromJson(new String(decodedBytes, StandardCharsets.UTF_8), UserAccountData.class);
			return Optional.of(decryptedInfo);
	}

}
