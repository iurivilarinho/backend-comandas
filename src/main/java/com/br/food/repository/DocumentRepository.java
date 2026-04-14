package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

}
