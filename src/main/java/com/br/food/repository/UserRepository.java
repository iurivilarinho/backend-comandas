package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.br.food.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	Optional<User> findByLogin(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByCpf(String cpf);

	Optional<User> findByGoogleSubject(String googleSubject);
}
