package com.br.food.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.Event;
import com.br.food.repository.EventRepository;

@Service
public class EventService {

	private final EventRepository eventoRepository;

	public EventService(EventRepository eventoRepository) {
		this.eventoRepository = eventoRepository;
	}

	@Transactional(readOnly = true)
	public Event findOpenEvent() {
		Optional<Event> event = eventoRepository
				.findFirstByHoraAberturaLessThanEqualAndHoraFimGreaterThanEqualOrHoraFimIsNullOrderByHoraAberturaDesc(
						LocalDateTime.now(), LocalDateTime.now());
		return event.orElse(null);
	}
}
