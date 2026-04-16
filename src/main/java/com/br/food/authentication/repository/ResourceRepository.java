package com.br.food.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.authentication.models.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

}

