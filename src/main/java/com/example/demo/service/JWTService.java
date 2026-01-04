package com.example.demo.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Service
public class JWTService {
	private String secretKeyString = "oa0vmWwk0ItyBcFmxmgWNw+1A9TDwzEFXBrcpIvId5I=";

	public String generateToken(String username) {
		// TODO Auto-generated method stub
		Map<String, Object> claims = new HashMap<>();
	    long expirationTimeMillis = 30 * 60 * 60 * 1000; // 30 hours in milliseconds
	    Date expirationDate = new Date(System.currentTimeMillis() + expirationTimeMillis);

	    System.out.println("Token will expire at: " + expirationDate); 
		return Jwts.builder()
				.claims()
				.add(claims)
				.subject(username)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(expirationDate)
				.and()
				.signWith(getKey())
				.compact();
	}
	private SecretKey getKey() {
		byte[] bytkey = Decoders.BASE64.decode(secretKeyString);
		return Keys.hmacShaKeyFor(bytkey);
	}
	public String extractUserName(String token) {
		// TODO Auto-generated method stub
		return extractClaim(token,Claims::getSubject);
	}
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
