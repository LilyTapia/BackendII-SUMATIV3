package com.letrasypapeles.backend.security;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilTest {

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtUtil jwtUtil;

    private Cliente cliente;
    private Role roleCliente;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        // Configurar la clave secreta para las pruebas (debe ser válida para Base64)
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "bGV0cmFzeXBhcGVsZXNTZWNyZXRLZXlXaXRoQXRMZWFzdDI1NkJpdHNUb0JlU2VjdXJlRW5vdWdoRm9ySE1BQ1NIQTI1NkFsZ29yaXRobQ==");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000); // 24 horas

        // Configurar roles
        roleCliente = new Role();
        roleCliente.setNombre("CLIENTE");

        roleAdmin = new Role();
        roleAdmin.setNombre("ADMIN");

        // Configurar cliente
        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);
        
        cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .apellido("Usuario")
                .email("test@example.com")
                .contraseña("password123")
                .roles(roles)
                .build();
    }

    @Test
    void generateJwtToken_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // When
        String token = jwtUtil.generateJwtToken(authentication);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(authentication, times(1)).getPrincipal();
        verify(userDetails, times(1)).getUsername();
    }

    @Test
    void getUsernameFromToken_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtil.generateJwtToken(authentication);

        // When
        String username = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    void validateJwtToken_ValidToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtil.generateJwtToken(authentication);

        // When
        boolean isValid = jwtUtil.validateJwtToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtUtil.validateJwtToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ExpiredToken() {
        // Given - Configurar expiración muy corta para la prueba
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 1); // 1 ms
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtil.generateJwtToken(authentication);

        // Esperar a que expire el token
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtUtil.validateJwtToken(token);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_NullToken() {
        // When
        boolean isValid = jwtUtil.validateJwtToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_EmptyToken() {
        // When
        boolean isValid = jwtUtil.validateJwtToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void getExpirationDateFromToken_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtil.generateJwtToken(authentication);

        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void generateTokenFromUsername_Success() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtUtil.generateTokenFromUsername(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateTokenWithRoles_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_CLIENTE"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // When
        String token = jwtUtil.generateJwtToken(authentication);

        // Then
        assertNotNull(token);
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void validateJwtToken_MalformedToken() {
        // Given
        String malformedToken = "malformed.token";

        // When
        boolean isValid = jwtUtil.validateJwtToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_UnsupportedToken() {
        // Given
        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";

        // When
        boolean isValid = jwtUtil.validateJwtToken(unsupportedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_IllegalArgumentToken() {
        // Given
        String illegalToken = "   ";

        // When
        boolean isValid = jwtUtil.validateJwtToken(illegalToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getClaimFromToken_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtil.generateJwtToken(authentication);

        // When
        String subject = jwtUtil.getClaimFromToken(token, claims -> claims.getSubject());

        // Then
        assertEquals("test@example.com", subject);
    }

    @Test
    void getClaimFromToken_ExpirationDate() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtil.generateJwtToken(authentication);

        // When
        Date expiration = jwtUtil.getClaimFromToken(token, claims -> claims.getExpiration());

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateJwtToken_SecurityException() {
        // Given - Token with wrong signature
        String tokenWithWrongSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.wrong_signature";

        // When
        boolean isValid = jwtUtil.validateJwtToken(tokenWithWrongSignature);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ExpiredTokenWithExpiredJwtException() {
        // Given - Create an already expired token
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -86400000); // -24 hours (expired)
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String expiredToken = jwtUtil.generateJwtToken(authentication);

        // Reset to normal expiration
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000);

        // When
        boolean isValid = jwtUtil.validateJwtToken(expiredToken);

        // Then
        assertFalse(isValid);
    }


}
