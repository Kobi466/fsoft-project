package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<Auth, UUID> {
    boolean existsByEmailOrPhone(String email, String phone);

    Optional<Auth> findByEmail(String email);

    Optional<Auth> findById(UUID id);

    Page<Auth> findAllByRoles_Name(String roleName, Pageable pageable);
}
