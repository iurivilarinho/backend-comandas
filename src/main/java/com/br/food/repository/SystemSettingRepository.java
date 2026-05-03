package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.SystemSetting;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

	Optional<SystemSetting> findByKey(String key);
}
