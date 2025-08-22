package io.sci.citizen.api.component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.sci.citizen.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

    public String createToken(User user) {
        var roles = String.join(", ", user.getRoles());
        JwtBuilder builder = Jwts.builder()
                .id(String.valueOf(user.getId()))
                .issuer(user.getUsername())
                .subject(roles)
                .setIssuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), SignatureAlgorithm.HS256);
        if (expiration >= 0) {
            builder.setExpiration(Date.from(Instant.now().plusSeconds(expiration * 60))); // 15 minutes
        }
        return builder.compact();
	}

	public boolean authenticate(String token) {
		try {
            if (parseClaims(token)!=null) {
                return true;
            }
		} catch (Exception e) {
            e.printStackTrace();
		}
        return false;
	}

    public boolean isValid(String token, UserDetails user) {
        try {
            var claims = parseClaims(token);
            boolean expired = false;
            if (claims.getExpiration()!=null) {
                expired = !claims.getExpiration().after(new Date());
            }
            return user.getUsername().equals(claims.getIssuer()) && !expired;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

	public String getUsername(String token) {
        return parseClaims(token).getIssuer();
	}

	public String getRoles(String token) {
		return parseClaims(token).getSubject();
	}

	public String getUserId(String token) {
        return parseClaims(token).getId();
	}

    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(Base64.getDecoder().decode(secret))
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))).build()
                .parseClaimsJws(token)
                .getBody();
    }

}