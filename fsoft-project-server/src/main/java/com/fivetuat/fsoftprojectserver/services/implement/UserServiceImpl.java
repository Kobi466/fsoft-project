package com.fivetuat.fsoftprojectserver.services.implement;



import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.AuthMapper;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.RefreshTokenRepository;
import com.fivetuat.fsoftprojectserver.services.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    AuthRepository authRepository;
    AuthMapper authMapper;
    RefreshTokenRepository refreshTokenRepository;


    @Override
    public Auth getUserById(UUID id) {
        return this.authRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public void deleteUser(UUID id) {
        var user = this.authRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
        refreshTokenRepository.deleteByAuthId(id);
        this.authRepository.delete(user);
    }
}
