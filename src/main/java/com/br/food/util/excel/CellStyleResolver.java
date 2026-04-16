package com.br.food.util.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

@FunctionalInterface
public interface CellStyleResolver {
	void applyStyle(Workbook workbook, Cell cell, Object cellValue);
}
