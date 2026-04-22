package com.br.food.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.models.Document;
import com.br.food.repository.DocumentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DocumentService {

	private static final int MAX_IMAGE_DIMENSION = 1600;
	private static final float JPEG_COMPRESSION_QUALITY = 0.82f;

	private final DocumentRepository documentRepository;

	public DocumentService(DocumentRepository documentRepository) {
		this.documentRepository = documentRepository;
	}

	public Document convertToDocument(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}
		return buildDocument(file.getOriginalFilename(), file.getContentType(), file.getBytes());
	}

	public Set<Document> converterEmListaDocumento(Set<MultipartFile> files) throws IOException {
		if (files == null) {
			return null;
		}

		Set<Document> documents = new HashSet<>();
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			Document document = convertToDocument(file);
			if (document != null) {
				documents.add(document);
			}
		}
		return documents;
	}

	public Document converterEmDocumento(byte[] bytes, String nameFile) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		String lower = nameFile.toLowerCase();
		String contentType;
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
			contentType = "image/jpeg";
		} else if (lower.endsWith(".png")) {
			contentType = "image/png";
		} else if (lower.endsWith(".webp")) {
			contentType = "image/webp";
		} else {
			contentType = "application/octet-stream";
		}

		return new Document(nameFile, contentType, bytes);
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
				return buildDocument(fallbackName + extension, contentType, bytes);
			}
		} catch (IllegalArgumentException | IOException exception) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public Document buscarPorId(Long id) {
		return documentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Document nao encontrado para ID " + id));
	}

	private Document buildDocument(String originalFilename, String contentType, byte[] bytes) throws IOException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
			return new Document(originalFilename, contentType, bytes);
		}

		CompressedImage compressedImage = compressImage(originalFilename, contentType, bytes);
		if (compressedImage == null) {
			return new Document(originalFilename, contentType, bytes);
		}

		return new Document(compressedImage.fileName(), compressedImage.contentType(), compressedImage.bytes());
	}

	private CompressedImage compressImage(String originalFilename, String contentType, byte[] bytes) throws IOException {
		BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(bytes));
		if (sourceImage == null) {
			return null;
		}

		BufferedImage scaledImage = scaleImageIfNeeded(sourceImage);
		boolean preserveAlpha = scaledImage.getColorModel().hasAlpha();
		String formatName = preserveAlpha ? "png" : "jpeg";
		String normalizedContentType = preserveAlpha ? "image/png" : "image/jpeg";
		String normalizedFileName = normalizeFileName(originalFilename, preserveAlpha ? ".png" : ".jpg");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		if ("jpeg".equals(formatName)) {
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
			if (!writers.hasNext()) {
				return null;
			}

			ImageWriter writer = writers.next();
			try (MemoryCacheImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(outputStream)) {
				writer.setOutput(imageOutputStream);
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				if (writeParam.canWriteCompressed()) {
					writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					writeParam.setCompressionQuality(JPEG_COMPRESSION_QUALITY);
				}
				writer.write(null, new IIOImage(scaledImage, null, null), writeParam);
			} finally {
				writer.dispose();
			}
		} else {
			ImageIO.write(scaledImage, formatName, outputStream);
		}

		byte[] compressedBytes = outputStream.toByteArray();
		if (compressedBytes.length == 0) {
			return null;
		}

		return new CompressedImage(normalizedFileName, normalizedContentType, compressedBytes);
	}

	private BufferedImage scaleImageIfNeeded(BufferedImage sourceImage) {
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int largestDimension = Math.max(width, height);

		if (largestDimension <= MAX_IMAGE_DIMENSION) {
			return sourceImage;
		}

		double scaleFactor = (double) MAX_IMAGE_DIMENSION / largestDimension;
		int scaledWidth = Math.max(1, (int) Math.round(width * scaleFactor));
		int scaledHeight = Math.max(1, (int) Math.round(height * scaleFactor));
		int imageType = sourceImage.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D graphics = scaledImage.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.drawImage(sourceImage, 0, 0, scaledWidth, scaledHeight, null);
		} finally {
			graphics.dispose();
		}

		return scaledImage;
	}

	private String normalizeFileName(String originalFilename, String extension) {
		String baseName = originalFilename == null || originalFilename.isBlank() ? "image" : originalFilename;
		int extensionIndex = baseName.lastIndexOf('.');
		if (extensionIndex > 0) {
			baseName = baseName.substring(0, extensionIndex);
		}
		return baseName + extension;
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

	private record CompressedImage(String fileName, String contentType, byte[] bytes) {
	}
}
