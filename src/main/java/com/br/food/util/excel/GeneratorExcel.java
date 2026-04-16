package com.br.food.util.excel;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class GeneratorExcel {

	public <T> ByteArrayOutputStream gerar(List<T> dados) throws Exception {
		return gerar(dados, null);
	}

	public <T> ByteArrayOutputStream gerar(List<T> dados, Map<String, CellStyleResolver> resolvers) throws Exception {
		if (dados == null || dados.isEmpty()) {
			throw new IllegalArgumentException("Lista de dados não pode ser vazia");
		}

		try (Workbook workbook = new XSSFWorkbook()) {
			adicionarAba(workbook, "MeuRelatorio", dados, resolvers, false);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return outputStream;
		}
	}

	public ByteArrayOutputStream gerarAbas(Map<String, ? extends List<?>> abas) throws Exception {
		return gerarAbas(abas, null);
	}

	public ByteArrayOutputStream gerarAbas(Map<String, ? extends List<?>> abas,
			Map<String, Map<String, CellStyleResolver>> resolversPorAba) throws Exception {
		if (abas == null || abas.isEmpty()) {
			throw new IllegalArgumentException("Mapa de abas não pode ser vazio");
		}

		try (Workbook workbook = new XSSFWorkbook()) {
			for (Map.Entry<String, ? extends List<?>> aba : abas.entrySet()) {
				List<?> dados = aba.getValue();
				if (dados == null || dados.isEmpty()) {
					continue;
				}
				Map<String, CellStyleResolver> resolvers = resolversPorAba != null ? resolversPorAba.get(aba.getKey())
						: null;
				adicionarAba(workbook, aba.getKey(), dados, resolvers, true);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return outputStream;
		}
	}

	private void adicionarAba(Workbook workbook, String nomeAba, List<?> dados, Map<String, CellStyleResolver> resolvers,
			boolean destacarLinhas) throws IllegalAccessException {
		Sheet sheet = workbook.createSheet(nomeAba);

		CellStyle headerStyle = criarHeaderStyle(workbook);
		CellStyle defaultDataStyle = criarDefaultDataStyle(workbook, new Color(255, 255, 255));
		CellStyle alternateDataStyle = criarDefaultDataStyle(workbook, new Color(244, 248, 246));

		List<Field> fields = obterCamposExportaveis(dados.get(0).getClass());
		if (fields.isEmpty()) {
			throw new IllegalArgumentException("Classe informada n\u00e3o possui campos export\u00e1veis para o Excel");
		}

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < fields.size(); i++) {
			Cell headerCell = headerRow.createCell(i);
			Field field = fields.get(i);
			ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
			headerCell.setCellValue(excelColumn != null ? excelColumn.value() : field.getName());
			headerCell.setCellStyle(headerStyle);
		}

		int linha = 1;
		for (Object item : dados) {
			Row dataRow = sheet.createRow(linha);

			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);
				field.setAccessible(true);
				Object value = field.get(item);

				Cell cell = dataRow.createCell(i);
				cell.setCellStyle(destacarLinhas && linha % 2 == 0 ? alternateDataStyle : defaultDataStyle);

				if (value instanceof String stringValue) {
					cell.setCellValue(stringValue);
				} else if (value instanceof Number numberValue) {
					cell.setCellValue(numberValue.doubleValue());
				} else if (value instanceof Boolean booleanValue) {
					cell.setCellValue(booleanValue);
				} else if (value instanceof java.util.Date dateValue) {
					cell.setCellValue(dateValue);
				} else if (value != null) {
					cell.setCellValue(value.toString());
				} else {
					cell.setCellValue("");
				}

				if (resolvers != null) {
					CellStyleResolver resolver = resolvers.get(field.getName());
					if (resolver != null) {
						resolver.applyStyle(workbook, cell, value);
					}
				}
			}
			linha++;
		}

		sheet.setAutoFilter(new CellRangeAddress(0, linha - 1, 0, fields.size() - 1));
		for (int i = 0; i < fields.size(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private List<Field> obterCamposExportaveis(Class<?> classe) {
		return Arrays.stream(classe.getDeclaredFields())
				.filter(field -> !field.isSynthetic())
				.filter(field -> possuiAnotacaoExcelColumn(field) || !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))
				.collect(Collectors.toList());
	}

	private boolean possuiAnotacaoExcelColumn(Field field) {
		return field.getAnnotation(ExcelColumn.class) != null;
	}

	private CellStyle criarHeaderStyle(Workbook workbook) {
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setWrapText(true);

		Color verdeCantoDeMinas = new Color(0, 130, 70);
		XSSFCellStyle xssfHeaderStyle = (XSSFCellStyle) headerStyle;
		xssfHeaderStyle.setFillForegroundColor(new XSSFColor(verdeCantoDeMinas, null));
		xssfHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		return headerStyle;
	}

	private CellStyle criarDefaultDataStyle(Workbook workbook, Color background) {
		CellStyle defaultDataStyle = workbook.createCellStyle();
		defaultDataStyle.setAlignment(HorizontalAlignment.CENTER);
		defaultDataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		defaultDataStyle.setWrapText(true);
		defaultDataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		defaultDataStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		defaultDataStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		defaultDataStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		XSSFCellStyle xssfDataStyle = (XSSFCellStyle) defaultDataStyle;
		xssfDataStyle.setFillForegroundColor(new XSSFColor(background, null));
		xssfDataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return defaultDataStyle;
	}
}
