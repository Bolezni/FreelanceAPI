package com.bolezni.repository;

import com.bolezni.model.UserDeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceTokenEntity, Long> {
    Optional<UserDeviceTokenEntity> findByDeviceToken(String deviceToken);
}
