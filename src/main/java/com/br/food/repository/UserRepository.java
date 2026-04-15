package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.acesso.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByLogin(String login);
}

