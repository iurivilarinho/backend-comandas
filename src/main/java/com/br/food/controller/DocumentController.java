package com.br.food.controller;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.models.Document;
import com.br.food.response.DocumentResponse;
import com.br.food.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Endpoints for stored documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@Operation(summary = "Find document by id")
	@GetMapping("/{id}")
	public ResponseEntity<DocumentResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new DocumentResponse(documentService.buscarPorId(id)));
	}

	@Operation(summary = "Get document binary content by id")
	@GetMapping("/{id}/content")
	public ResponseEntity<byte[]> findContentById(
			@PathVariable Long id,
			@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
		Document document = documentService.buscarPorId(id);
		String eTag = "\"" + DigestUtils.md5DigestAsHex(
				(id + "|" + document.getSize() + "|" + document.getName() + "|" + document.getContentType())
						.getBytes(StandardCharsets.UTF_8))
				+ "\"";

		if (eTag.equals(ifNoneMatch)) {
			return ResponseEntity.status(304)
					.eTag(eTag)
					.cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
					.build();
		}

		MediaType mediaType = document.getContentType() != null && !document.getContentType().isBlank()
				? MediaType.parseMediaType(document.getContentType())
				: MediaType.APPLICATION_OCTET_STREAM;

		return ResponseEntity.ok()
				.contentType(mediaType)
				.contentLength(document.getDocument() != null ? document.getDocument().length : 0)
				.eTag(eTag)
				.cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
				.body(document.getDocument());
	}
}
