package com.uow.FYP_23_S1_11.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.uow.FYP_23_S1_11.enums.ETokenType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    @Value("${refresh.jwtsecret}") private String refreshTokenSecret;
    @Value("${refresh.jwtexpirationms}") private int refreshTokenExpiry;
    @Value("${access.jwtsecret}") private String accessTokenSecret;
    @Value("${access.jwtexpirationms}") private int accessTokenExpiry;

    public String getUserFromToken(ETokenType type, String token) {
        return extractClaims(type, token, Claims::getSubject);
    }

    public String genereteToken(ETokenType type, UserDetails userDetails) {
        return generateToken(type, userDetails, new HashMap<>());
    }

    public String generateToken(ETokenType type, UserDetails userDetails, Map<String, Object> extraClaims) {
        int expiry = 0;
        if(type == ETokenType.ACCESS_TOKEN) {
            expiry = accessTokenExpiry;
        } else if(type == ETokenType.REFRESH_TOKEN) {
            expiry = refreshTokenExpiry;
        }

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSignInKey(type), SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T extractClaims(ETokenType type, String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(type, token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(ETokenType type, String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(ETokenType type) {
        String secret = "";
        if(type == ETokenType.ACCESS_TOKEN) {
            secret = accessTokenSecret;
        } else if(type == ETokenType.REFRESH_TOKEN) {
            secret = refreshTokenSecret;
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
}