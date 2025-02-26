package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Documento;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

}
