package com.placement.portal.service.auth;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.Role;
import com.placement.portal.dto.request.LoginRequest;
import com.placement.portal.dto.request.RegisterRequest;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.PlacementOfficerProfileRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.repository.UserRepository;
import com.placement.portal.security.jwt.JwtProperties;
import com.placement.portal.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Locks the following fixes:</p>
 * <ul>
 *   <li><b>B2</b> — {@code AuthResponse.email} is populated from the persisted user.</li>
 *   <li><b>B6</b> — Registering an EMPLOYER seeds {@code companyName} with the user's full name
 *       instead of an empty string.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private EmployerProfileRepository employerProfileRepository;
    @Mock private FacultyProfileRepository facultyProfileRepository;
    @Mock private PlacementOfficerProfileRepository placementOfficerProfileRepository;
    @Mock private Authentication authentication;

    private final JwtProperties jwtProperties = new JwtProperties();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                jwtTokenProvider,
                refreshTokenService,
                userRepository,
                passwordEncoder,
                jwtProperties,
                studentProfileRepository,
                employerProfileRepository,
                facultyProfileRepository,
                placementOfficerProfileRepository
        );
    }

    @Test
    void login_returnsEmailInAuthResponse() {
        LoginRequest req = LoginRequest.builder()
                .email("ada@example.com")
                .password("hunter2")
                .build();

        User user = User.builder()
                .id("user-1")
                .email("ada@example.com")
                .fullName("Ada Lovelace")
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("raw-refresh");

        AuthService.AuthResult result = authService.loginWithToken(req);

        assertThat(result.authResponse().getEmail())
                .as("B2: AuthResponse must echo the authenticated user's email")
                .isEqualTo("ada@example.com");
        assertThat(result.authResponse().getFullName()).isEqualTo("Ada Lovelace");
        assertThat(result.authResponse().getRole()).isEqualTo("STUDENT");
        assertThat(result.rawRefreshToken()).isEqualTo("raw-refresh");
    }

    @Test
    void registerEmployer_seedsCompanyNameFromFullName() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("acme@example.com");
        req.setPassword("hunter2");
        req.setFullName("Acme Corp HR");
        req.setRole(Role.EMPLOYER);

        when(userRepository.existsByEmail("acme@example.com")).thenReturn(false);
        when(passwordEncoder.encode("hunter2")).thenReturn("hashed");
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("raw-refresh");

        authService.registerWithToken(req);

        ArgumentCaptor<EmployerProfile> captor = ArgumentCaptor.forClass(EmployerProfile.class);
        org.mockito.Mockito.verify(employerProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getCompanyName())
                .as("B6: new employer profile must seed companyName, not leave it blank")
                .isEqualTo("Acme Corp HR");
    }
}
