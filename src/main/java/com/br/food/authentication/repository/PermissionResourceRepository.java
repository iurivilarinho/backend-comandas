package com.br.food.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.br.food.authentication.models.PermissionResource;

@Repository
public interface PermissionResourceRepository
		extends JpaRepository<PermissionResource, Long>, JpaSpecificationExecutor<PermissionResource> {

}

