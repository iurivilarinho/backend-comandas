package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.acesso.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

}
