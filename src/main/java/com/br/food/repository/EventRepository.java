package com.br.food.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	Optional<Event> findFirstByHoraAberturaLessThanEqualAndHoraFimGreaterThanEqualOrHoraFimIsNullOrderByHoraAberturaDesc(
			LocalDateTime now1, LocalDateTime now2);
}
