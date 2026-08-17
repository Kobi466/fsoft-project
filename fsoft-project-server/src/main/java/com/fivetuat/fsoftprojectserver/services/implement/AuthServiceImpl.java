package com.fivetuat.fsoftprojectserver.services.implement;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fivetuat.fsoftprojectserver.dtos.request.AuthenticatedRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ForgotPasswordRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.IntrospectTokenRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.LogoutRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.RefreshTokenRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ResetPasswordRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.UserCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AuthenticatedResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.IntrospectTokenResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.TokenResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.entities.authentication.RefreshToken;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.Roles;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.httpClients.GoogleOauth2Client;
import com.fivetuat.fsoftprojectserver.httpClients.GoogleUserInfoClient;
import com.fivetuat.fsoftprojectserver.mappers.AuthMapper;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.ProfileRepository;
import com.fivetuat.fsoftprojectserver.repositories.RefreshTokenRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.JwtService;
import com.fivetuat.fsoftprojectserver.services.NotificationService;
import com.fivetuat.fsoftprojectserver.services.RedisService;
import com.fivetuat.fsoftprojectserver.services.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {
    AuthMapper authMapper;
    RoleService roleService;
    AuthRepository authRepository;
    ProfileRepository profileRepository;
    RefreshTokenRepository refreshTokenRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    JwtService jwtService;
    RedisService redisService;
    NotificationService notificationService;
    GoogleOauth2Client googleOauth2Client;
    GoogleUserInfoClient googleUserInfoClient;
    @NonFinal
    @Value("${google.client-id}")
    String clientId;
    @NonFinal
    @Value("${google.client-secret}")
    String clientSecret;
    @NonFinal
    @Value("${google.grant-type}")
    String grantType;
    @NonFinal
    @Value("${google.redirect-uri}")
    String redirectUrl;

    @Override
    public Profile getCurrentProfile() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UUID userId = UUID.fromString(name);
            return profileRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Transactional
    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        this.checkEmailPhone(userCreateRequest.getEmail(), userCreateRequest.getPhone());
        var auth = authMapper.toAuth(userCreateRequest);
        auth.setPasswordHash(bCryptPasswordEncoder.encode(userCreateRequest.getPassWord()));
        auth.getProfile().setAuth(auth);
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.getRoleByName(Roles.USER.name()));
        auth.setRoles(roles);
        var response = authRepository.save(auth);

        String userName = auth.getProfile().getFullName();
        this.notificationService.sendWelcomeEmail(auth.getEmail(), userName);
        return authMapper.toUserResponse(response);
    }

    @Override
    public AuthenticatedResponse authenticated(AuthenticatedRequest authenticatedRequest) {
        Optional<Auth> optionalAuth = this.authRepository.findByEmail((authenticatedRequest.getEmail()));
        if (optionalAuth.isEmpty()) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        var auth = optionalAuth.get();
        if (!bCryptPasswordEncoder.matches(authenticatedRequest.getPassword(), auth.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);
        var refreshTokenEntity = this.refreshTokenRepository.save(RefreshToken.builder()
                .auth(auth)
                .token(refreshToken)
                .issueTime(this.jwtService.getIssuedAtDateFromToken(refreshToken))
                .expiryTime(this.jwtService.getExpirationDateFromToken(refreshToken))
                .build()
        );
        return AuthenticatedResponse.builder()
                .user(this.authMapper.toUserResponse(auth))
                .token(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .accessExpiresAt(jwtService.getExpirationDateFromToken(accessToken))
                        .refreshExpiresAt(refreshTokenEntity.getExpiryTime())
                        .accessIssuedAt(jwtService.getIssuedAtDateFromToken(accessToken))
                        .refreshIssuedAt(refreshTokenEntity.getIssueTime())
                        .accessExpirationTime(jwtService.getExpirationTimeFromToken(accessToken))
                        .refreshExpirationTime(jwtService.getExpirationTimeFromToken(refreshToken))
                        .build()
                )
                .build();
    }

    @Override
    public AuthenticatedResponse authenticatedUserGoogle(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", grantType);
        formData.add("redirect_uri", redirectUrl);
        formData.add("code", code);
        var tokenResponse = this.googleOauth2Client.exchangeToken(formData);
        log.info("tokenResponse={}", tokenResponse);
        var userGG = this.googleUserInfoClient.getUserInfo("json",
                Objects.requireNonNull(tokenResponse)
                        .getAccessToken()
        );
        log.info("userGG={}", userGG);

        Set<Role> roles = new HashSet<>();
        roles.add(roleService.getRoleByName(Roles.USER.name()));

        var auth = this.authRepository.findByEmail(userGG.getEmail()).orElseGet(
                () -> {
                    var auths = Auth.builder()
                            .email(userGG.getEmail())
                            .build();
                    auths.setRoles(roles);
                    auths.setProfile(Profile.builder().auth(auths)
                            .fullName(userGG.getName())
                            .avatar(userGG.getPicture())
                            .build());
                    var savedAuth = this.authRepository.save(auths);
                    this.notificationService.sendWelcomeEmail(savedAuth.getEmail(),userGG.getName());
                    return savedAuth;
                }
        );
        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);
        var refreshTokenEntity = this.refreshTokenRepository.save(RefreshToken.builder()
                .auth(auth)
                .token(refreshToken)
                .issueTime(this.jwtService.getIssuedAtDateFromToken(refreshToken))
                .expiryTime(this.jwtService.getExpirationDateFromToken(refreshToken))
                .build()
        );
        return AuthenticatedResponse.builder()
                .user(this.authMapper.toUserResponse(auth))
                .token(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .accessExpiresAt(jwtService.getExpirationDateFromToken(accessToken))
                        .refreshExpiresAt(refreshTokenEntity.getExpiryTime())
                        .accessIssuedAt(jwtService.getIssuedAtDateFromToken(accessToken))
                        .refreshIssuedAt(refreshTokenEntity.getIssueTime())
                        .accessExpirationTime(jwtService.getExpirationTimeFromToken(accessToken))
                        .refreshExpirationTime(jwtService.getExpirationTimeFromToken(refreshToken))
                        .build()
                )
                .build();
    }

//    @PreAuthorize("hasRole('USER')")
//    @Transactional
//    @Override
//    public void deleteAccount() {
//        Long currentUserId = profileServiceImpl.getId();
//        refreshTokenRepository.deleteByAuthId(currentUserId);
//        authRepository.deleteById(currentUserId);
//    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        this.authRepository.findByEmail(request.getEmail()).ifPresent(auth -> {
            String otpValue = generateOtp();
            String redisKey = "password-reset-otp::" + auth.getEmail();
            redisService.saveOtp(redisKey, otpValue, 10);
            notificationService.sendForgotPasswordEmail(auth.getEmail(), auth.getProfile().getFullName(), otpValue);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        this.authRepository.findByEmail(request.getEmail()).ifPresent(auth -> {
            String redisKey = "password-reset-otp::" + auth.getEmail();
            String otpValue = redisService.getOtp(redisKey);
            if (otpValue == null || !otpValue.equals(request.getOtp())) {
                throw new AppException(ErrorCode.INVALID_OTP);
            }
            auth.setPasswordHash(bCryptPasswordEncoder.encode(request.getNewPassword()));
            authRepository.save(auth);

            redisService.deleteOtp(redisKey);
        });
    }

    @Override
    public Auth findByEmail(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateOtp() {
        return new DecimalFormat("000000").format(new Random().nextInt(999999));
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        Optional<RefreshToken> optionalRefreshToken = this.refreshTokenRepository
                .findByToken(refreshTokenRequest.getRefreshToken());
        if (optionalRefreshToken.isEmpty()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        RefreshToken refreshToken = optionalRefreshToken.get();
        if (this.jwtService.validateToken(refreshToken.getToken()) && refreshToken.isRevoked()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_FAILED);
        }
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        if (id.equals(refreshToken.getAuth().getId().toString())) refreshToken.setRevoked(true);
        Optional<Auth> optionalAuth = this.authRepository.findById(UUID.fromString(id));
        if (optionalAuth.isEmpty()) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        var auth = optionalAuth.get();
        String newAccessToken = this.jwtService.generateAccessToken(auth);
        String newRefreshToken = this.jwtService.generateRefreshToken(auth);
        var newRefreshTokenEntity = this.refreshTokenRepository.save(RefreshToken.builder()
                .auth(auth)
                .token(newRefreshToken)
                .issueTime(this.jwtService.getIssuedAtDateFromToken(newRefreshToken))
                .expiryTime(this.jwtService.getExpirationDateFromToken(newRefreshToken))
                .build()
        );
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessExpiresAt(this.jwtService.getExpirationDateFromToken(newAccessToken))
                .refreshExpiresAt(newRefreshTokenEntity.getExpiryTime())
                .accessIssuedAt(this.jwtService.getIssuedAtDateFromToken(newAccessToken))
                .refreshIssuedAt(newRefreshTokenEntity.getIssueTime())
                .accessExpirationTime(this.jwtService.getExpirationTimeFromToken(newAccessToken))
                .refreshExpirationTime(this.jwtService.getExpirationTimeFromToken(newRefreshToken))
                .build();
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        if (!this.jwtService.validateToken(logoutRequest.getAccessToken())) {
            throw new AppException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        redisService.blackListAccessToken(logoutRequest.getAccessToken()
                , this.jwtService.getExpirationTimeFromToken(logoutRequest.getAccessToken())
        );
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<RefreshToken> listRefreshToken = this.refreshTokenRepository.findByAuthId(UUID.fromString(id));
        listRefreshToken.forEach(refreshToken -> refreshToken.setRevoked(true));
        this.refreshTokenRepository.saveAll(listRefreshToken);
    }

    @Override
    public IntrospectTokenResponse introspectToken(IntrospectTokenRequest request) {
        return IntrospectTokenResponse.builder()
                .result(this.jwtService.validateToken(request.getToken()))
                .build();
    }


    private void checkEmailPhone(String email, String phone) {
        if (this.authRepository.existsByEmailOrPhone(email, phone)) {
            throw new AppException(ErrorCode.EMAIL_OR_PHONE_EXISTS);
        }
    }

    @Override
    public void createAuthAdmin(Auth auth) {
        Optional<Auth> optionalAuth = this.authRepository.findByEmail(auth.getEmail());
        if (optionalAuth.isPresent()) {
            return;
        }
        Profile profile = Profile.builder()
                .fullName("Admin")
                .build();
        profile.setAuth(auth);
        auth.setProfile(profile);
        auth.setPasswordHash(bCryptPasswordEncoder.encode(auth.getPasswordHash()));
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.getRoleByName(Roles.ADMIN.name()));
        auth.setRoles(roles);
        authRepository.save(auth);
    }
}
