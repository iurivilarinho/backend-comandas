package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.acesso.Perfil;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

}
