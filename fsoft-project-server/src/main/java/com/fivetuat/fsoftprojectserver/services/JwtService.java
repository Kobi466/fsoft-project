package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import org.springframework.stereotype.Service;

import java.util.Date;

public interface JwtService {
    String generateAccessToken(Auth auth);

    String generateRefreshToken(Auth auth);

    boolean validateToken(String token);

    String getIdFromToken(String token);

    Date getExpirationDateFromToken(String token);

    Date getIssuedAtDateFromToken(String token);

    long getExpirationTimeFromToken(String token);
}
