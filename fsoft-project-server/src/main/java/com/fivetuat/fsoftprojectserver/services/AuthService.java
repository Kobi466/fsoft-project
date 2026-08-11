package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.*;
import com.fivetuat.fsoftprojectserver.dtos.response.AuthenticatedResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.IntrospectTokenResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.TokenResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserResponse;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.entities.Profile;

public interface AuthService {

    Profile getCurrentProfile();

    UserResponse createUser(UserCreateRequest userCreateRequest);

    AuthenticatedResponse authenticated(AuthenticatedRequest authenticatedRequest);

//    AuthenticatedResponse authenticatedUserGoogle(String code);

    TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(LogoutRequest logoutRequest);

    IntrospectTokenResponse introspectToken(IntrospectTokenRequest request);

    void createAuthAdmin(Auth auth);

//    void deleteAccount();

    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    void resetPassword(ResetPasswordRequest request);
}
