package com.br.food.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Evento;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

	Optional<Evento> findFirstByHoraAberturaLessThanEqualAndHoraFimGreaterThanEqualOrHoraFimIsNullOrderByHoraAberturaDesc(
			LocalDateTime now1, LocalDateTime now2);
}
