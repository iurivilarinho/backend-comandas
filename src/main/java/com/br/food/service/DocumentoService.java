package com.br.food.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.models.Documento;
import com.br.food.repository.DocumentoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DocumentoService {

	@Autowired
	public DocumentoRepository documentoRepository;

	public Documento converterEmDocumento(MultipartFile file, Boolean comprimir) throws IOException {
		if (file != null) {
			return new Documento(file.getOriginalFilename(), file.getContentType(),
					comprimir ? compressImage(file) : file.getBytes());
		}
		return null;
	}

	public Set<Documento> converterEmListaDocumento(Set<MultipartFile> file) throws IOException {

		if (file != null) {
			Set<Documento> documentos = new HashSet<>();
			file.forEach(f -> {
				if (!f.isEmpty()) {
					try {
						documentos.add(new Documento(f.getOriginalFilename(), f.getContentType(), f.getBytes()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			return documentos;
		} else {
			return null;
		}

	}

	@Transactional(readOnly = true)
	public Documento bsucarPorId(Long id) {
		return documentoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Documento não encontrado para ID " + id));
	}

	private byte[] compressImage(MultipartFile file) throws IOException {
		// Obtém o formato do tipo MIME
		String contentType = file.getContentType().contains("png") ? "originalimage/jpeg" : file.getContentType();

		if (contentType == null || (!contentType.contains("jpeg") && !contentType.contains("png"))) {
			throw new IllegalArgumentException("Formato de arquivo não suportado: " + contentType);
		}

		// Define o formato baseado no tipo MIME
		String format = contentType.contains("jpeg") ? "jpeg" : "png";

		// Lê a imagem original
		BufferedImage originalImage = ImageIO.read(file.getInputStream());

		// Converte para RGB, se necessário
		BufferedImage rgbImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = rgbImage.createGraphics();

		graphics.setPaint(java.awt.Color.WHITE);
		graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());

		graphics.drawImage(originalImage, 0, 0, null);
		graphics.dispose();

		// Configura o stream para salvar a imagem compactada
		ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();

		try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressedOutputStream)) {
			// Configura o escritor de imagem para o formato especificado
			ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
			writer.setOutput(outputStream);

			// Configura os parâmetros de compactação
			ImageWriteParam param = writer.getDefaultWriteParam();
			if ("jpeg".equalsIgnoreCase(format)) {
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality((float) 0.7); // Qualidade entre 0.0 (ruim) e 1.0 (ótima)
			}

			// Escreve a imagem compactada no stream
			writer.write(null, new javax.imageio.IIOImage(rgbImage, null, null), param);
			writer.dispose();
		}

		// Retorna os bytes compactados
		return compressedOutputStream.toByteArray();
	}

	@Transactional
	public void createDocument(Documento documento) {
		documentoRepository.save(documento);
	}

}
