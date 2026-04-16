package com.br.food.authentication.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.authentication.models.RecoveryPassword;

@Repository
public interface RecoveryPasswordRepository extends JpaRepository<RecoveryPassword, Long> {

	boolean existsByUserIdAndCodeAndExpirationDateAfter(Long id, String code, LocalDateTime now);

	Optional<RecoveryPassword> findFirstByUserIdAndCodeAndExpirationDateAfter(Long id, String code, LocalDateTime now);

	void deleteByUserId(Long id);

}
