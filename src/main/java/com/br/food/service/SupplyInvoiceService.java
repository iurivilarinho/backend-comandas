package com.br.food.service;

import java.io.IOException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.enums.Types.SupplyInvoiceStatus;
import com.br.food.models.Document;
import com.br.food.models.StockEntry;
import com.br.food.models.SupplyInvoice;
import com.br.food.models.Product;
import com.br.food.repository.SupplyInvoiceRepository;
import com.br.food.request.StockEntryRequest;
import com.br.food.request.SupplyInvoiceRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SupplyInvoiceService {

	private final SupplyInvoiceRepository supplyInvoiceRepository;
	private final ProductService productService;
	private final DocumentService documentService;

	public SupplyInvoiceService(
			SupplyInvoiceRepository supplyInvoiceRepository,
			ProductService productService,
			DocumentService documentService) {
		this.supplyInvoiceRepository = supplyInvoiceRepository;
		this.productService = productService;
		this.documentService = documentService;
	}

	@Transactional
	public SupplyInvoice create(SupplyInvoiceRequest request, MultipartFile attachment) throws IOException {
		validateUniqueAccessKey(request.getAccessKey());
		Document document = documentService.toDocument(attachment, false);
		SupplyInvoice invoice = new SupplyInvoice(request, document);
		fillInvoiceItems(invoice, request);
		return supplyInvoiceRepository.save(invoice);
	}

	@Transactional(readOnly = true)
	public SupplyInvoice findById(Long id) {
		return supplyInvoiceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Supply invoice not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<SupplyInvoice> findAll(Pageable pageable) {
		return supplyInvoiceRepository.findAll(pageable);
	}

	@Transactional
	public SupplyInvoice update(Long id, SupplyInvoiceRequest request, MultipartFile attachment) throws IOException {
		SupplyInvoice invoice = findById(id);
		if (!invoice.getAccessKey().equals(request.getAccessKey())) {
			validateUniqueAccessKey(request.getAccessKey());
		}

		Document document = attachment != null ? documentService.toDocument(attachment, false) : null;
		invoice.update(request, document);
		invoice.getItems().clear();
		fillInvoiceItems(invoice, request);
		return supplyInvoiceRepository.save(invoice);
	}

	@Transactional
	public void updateStatus(Long id, SupplyInvoiceStatus targetStatus) {
		SupplyInvoice invoice = findById(id);
		SupplyInvoiceStatus.validateTransition(invoice.getStatus(), targetStatus);
		invoice.setStatus(targetStatus);
	}

	@Transactional(readOnly = true)
	public void validateUniqueAccessKey(String accessKey) {
		supplyInvoiceRepository.findByAccessKeyAndStatusNot(accessKey, SupplyInvoiceStatus.CANCELED).ifPresent(invoice -> {
			throw new DataIntegrityViolationException("There is already an active supply invoice for this access key.");
		});
	}

	private void fillInvoiceItems(SupplyInvoice invoice, SupplyInvoiceRequest request) {
		for (StockEntryRequest item : request.getItems()) {
			Product product = productService.findById(item.getProductId());
			invoice.getItems().add(new StockEntry(item, product, invoice));
		}
	}
}
