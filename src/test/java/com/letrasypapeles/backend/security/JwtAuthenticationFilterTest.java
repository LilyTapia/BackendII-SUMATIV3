package com.letrasypapeles.backend.security;

import com.letrasypapeles.backend.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        userDetails = new User("test@example.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "test@example.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(usuarioService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validateJwtToken(token);
        verify(jwtUtil).getUsernameFromToken(token);
        verify(usuarioService).loadUserByUsername(username);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_ContinuesFilter() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateJwtToken(any());
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader_ContinuesFilter() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Invalid header");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateJwtToken(any());
    }

    @Test
    void doFilterInternal_EmptyAuthorizationHeader_ContinuesFilter() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateJwtToken(any());
    }

    @Test
    void doFilterInternal_InvalidToken_ContinuesFilter() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtil.validateJwtToken(token)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validateJwtToken(token);
        verify(jwtUtil, never()).getUsernameFromToken(any());
    }

    @Test
    void doFilterInternal_ExceptionThrown_ContinuesFilter() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtil.validateJwtToken(token)).thenThrow(new RuntimeException("JWT processing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_UserServiceException_ContinuesFilter() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "test@example.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(usuarioService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_BearerTokenWithSpaces_ParsesCorrectly() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "test@example.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer   " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(usuarioService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void parseJwt_ValidBearerToken_ReturnsToken() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "test@example.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(usuarioService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).validateJwtToken(token);
    }

    @Test
    void parseJwt_NullHeader_ReturnsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).validateJwtToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void parseJwt_EmptyHeader_ReturnsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).validateJwtToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void parseJwt_NonBearerToken_ReturnsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).validateJwtToken(any());
        verify(filterChain).doFilter(request, response);
    }
}
