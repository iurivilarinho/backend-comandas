package com.br.food.service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.DiningTable;
import com.br.food.repository.DiningTableRepository;
import com.br.food.request.DiningTableRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DiningTableService {

	private final DiningTableRepository diningTableRepository;

	public DiningTableService(DiningTableRepository diningTableRepository) {
		this.diningTableRepository = diningTableRepository;
	}

	@Transactional
	public List<DiningTable> createMany(Integer tableCount) {
		List<DiningTable> tables = new ArrayList<>();
		for (int index = 0; index < tableCount; index++) {
			tables.add(new DiningTable(generateTableNumber()));
		}
		return diningTableRepository.saveAll(tables);
	}

	@Transactional
	public DiningTable create(DiningTableRequest request) {
		return diningTableRepository.save(new DiningTable(request));
	}

	@Transactional(readOnly = true)
	public String generateTableNumber() {
		DiningTable highestTable = diningTableRepository.findTopByOrderByNumeroDesc();
		int nextNumber = highestTable != null ? Integer.parseInt(highestTable.getNumber()) + 1 : 1;
		return String.valueOf(nextNumber);
	}

	@Transactional
	public DiningTable update(Long id, DiningTableRequest request) {
		DiningTable table = findById(id);
		table.update(request);
		return diningTableRepository.save(table);
	}

	@Transactional(readOnly = true)
	public DiningTable findById(Long id) {
		return diningTableRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Table not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public DiningTable findByNumber(String number) {
		return diningTableRepository.findByNumero(number)
				.orElseThrow(() -> new EntityNotFoundException("Table not found for number " + number + "."));
	}

	@Transactional(readOnly = true)
	public List<DiningTable> findAll() {
		return diningTableRepository.findAll();
	}

	@Transactional
	public void updateStatus(Long id, Boolean active) {
		DiningTable table = findById(id);
		table.setStatus(active);
	}

	@Transactional
	public void reserveTable(Long id) throws AccessDeniedException {
		DiningTable table = findById(id);
		if (Boolean.TRUE.equals(table.getOccupied())) {
			throw new AccessDeniedException("Table is already occupied.");
		}
		table.setOccupied(true);
	}

	@Transactional
	public void releaseTable(Long id) {
		DiningTable table = findById(id);
		table.setOccupied(false);
	}
}
