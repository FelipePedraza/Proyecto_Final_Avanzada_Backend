package co.edu.uniquindio.application.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {

    @Value("${secretKey}")
    private String secretKey;

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

    public String generarRefreshToken(String id, Map<String, String> claims) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claims(claims)
                .subject(id)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(7L, ChronoUnit.DAYS))) // 7 Días
                .signWith(obtenerKey())
                .compact();
    }

    public Jws<Claims> decodificarJwt(String jwtString) throws ExpiredJwtException,
            UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
        JwtParser jwtParser = Jwts.parser().verifyWith(obtenerKey()).build();
        return jwtParser.parseSignedClaims(jwtString);
    }

    private SecretKey obtenerKey(){
        byte[] secretKeyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }


    public boolean validarToken(String token, UserDetails userDetails) {
        try {
            String username = decodificarJwt(token).getPayload().getSubject();
            return username.equals(userDetails.getUsername()) && !estaExpirado(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        Date expiration = decodificarJwt(token).getPayload().getExpiration();
        return expiration.before(new Date());
    }
}
