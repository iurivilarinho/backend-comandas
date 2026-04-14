package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.acesso.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}

