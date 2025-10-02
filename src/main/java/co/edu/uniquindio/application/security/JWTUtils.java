package co.edu.uniquindio.application.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {

    public String generarToken(String id, Map<String, String> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(id)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1L, ChronoUnit.HOURS)))
                .signWith(obtenerKey())
                .compact();
    }

    public Jws<Claims> decodificarJwt(String jwtString) throws ExpiredJwtException,
            UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
        JwtParser jwtParser = Jwts.parser().verifyWith(obtenerKey()).build();
        return jwtParser.parseSignedClaims(jwtString);
    }

    private SecretKey obtenerKey(){
        String secretKey = "vivigoeslamejorapp12345678904208302025";
        byte[] secretKeyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }
}
