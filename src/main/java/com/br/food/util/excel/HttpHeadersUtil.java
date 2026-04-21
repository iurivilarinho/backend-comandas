package com.br.food.util.excel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class HttpHeadersUtil {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

	public HttpHeaders excel(String fileName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
		headers.setContentDispositionFormData("attachment",
				fileName + LocalDateTime.now().format(DATE_TIME_FORMATTER) + ".xlsx");
		return headers;
	}

	public HttpHeaders pdf(String fileName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment",
				fileName + LocalDateTime.now().format(DATE_TIME_FORMATTER) + ".pdf");
		return headers;
	}
}
