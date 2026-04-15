package com.br.food.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.models.Document;
import com.br.food.repository.DocumentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DocumentService {

	private final DocumentRepository documentRepository;

	public DocumentService(DocumentRepository documentRepository) {
		this.documentRepository = documentRepository;
	}

	public Document convertToDocument(MultipartFile file) throws IOException {
		if (file != null) {
			return new Document(file);
		}
		return null;
	}

	public Set<Document> converterEmListaDocumento(Set<MultipartFile> file) throws IOException {

		if (file != null) {
			Set<Document> documents = new HashSet<>();
			file.forEach(f -> {
				if (!f.isEmpty()) {
					try {
						documents.add(new Document(f));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			return documents;
		} else {
			return null;
		}

	}

	public Document converterEmDocumento(byte[] bytes, String nameFile) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		Document doc = new Document();
		doc.setName(nameFile);
		doc.setDocument(bytes);
		doc.setSize((long) bytes.length);

		// Define o content type manualmente com base na extensão
		String lower = nameFile.toLowerCase();
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
			doc.setContentType("image/jpeg");
		} else if (lower.endsWith(".png")) {
			doc.setContentType("image/png");
		} else if (lower.endsWith(".webp")) {
			doc.setContentType("image/webp");
		} else {
			doc.setContentType("application/octet-stream"); // fallback
		}

		return doc;
	}

	public Document convertGoogleProfileImageToDocument(String imageUrl, String fallbackName) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return null;
		}

		try {
			URI uri = URI.create(imageUrl);
			URLConnection connection = uri.toURL().openConnection();
			String contentType = connection.getContentType();

			try (InputStream inputStream = connection.getInputStream();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				inputStream.transferTo(outputStream);
				byte[] bytes = outputStream.toByteArray();

				if (bytes.length == 0) {
					return null;
				}

				String extension = resolveExtension(contentType, imageUrl);
				Document document = converterEmDocumento(bytes, fallbackName + extension);
				if (document != null && contentType != null && !contentType.isBlank()) {
					document.setContentType(contentType);
				}
				return document;
			}
		} catch (IllegalArgumentException | IOException exception) {
			return null;
		}
	}

	private String resolveExtension(String contentType, String imageUrl) {
		if (contentType != null) {
			if ("image/jpeg".equalsIgnoreCase(contentType)) {
				return ".jpg";
			}
			if ("image/png".equalsIgnoreCase(contentType)) {
				return ".png";
			}
			if ("image/webp".equalsIgnoreCase(contentType)) {
				return ".webp";
			}
			if ("image/gif".equalsIgnoreCase(contentType)) {
				return ".gif";
			}
		}

		String lowerUrl = imageUrl.toLowerCase();
		if (lowerUrl.contains(".png")) {
			return ".png";
		}
		if (lowerUrl.contains(".webp")) {
			return ".webp";
		}
		if (lowerUrl.contains(".gif")) {
			return ".gif";
		}
		return ".jpg";
	}

	@Transactional
	public Document buscarPorId(Long id) {
		return documentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Document não encontrado para ID " + id));
	}
}
