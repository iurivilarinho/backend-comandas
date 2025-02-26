package com.br.food.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.Evento;
import com.br.food.repository.EventoRepository;

@Service
public class EventoService {

	@Autowired
	private EventoRepository eventoRepository;

	@Transactional
	public Evento validaEventoAberto() {
		Optional<Evento> evento = eventoRepository
				.findFirstByHoraAberturaLessThanEqualAndHoraFimGreaterThanEqualOrHoraFimIsNullOrderByHoraAberturaDesc(
						LocalDateTime.now(), LocalDateTime.now());

		if (evento.isPresent()) {
			return evento.get();
		} else {
			return null;
		}
	}

}
