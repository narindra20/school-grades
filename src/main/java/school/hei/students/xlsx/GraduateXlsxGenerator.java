package school.hei.students.xlsx;

import static java.io.File.createTempFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class GraduateXlsxGenerator {

  private static final String[] HEADERS = {"Rang", "STD", "Nom", "Prénom", "Moyenne générale"};

  @SneakyThrows
  public File generate(List<GraduateRow> rows) {
    var file = createTempFile("graduates-", ".xlsx");
    try (var workbook = new XSSFWorkbook();
        var out = new FileOutputStream(file)) {
      var sheet = workbook.createSheet("Diplômés");
      writeHeader(workbook, sheet);
      writeRows(sheet, rows);
      autoSizeColumns(sheet);
      workbook.write(out);
    }
    return file;
  }

  private void writeHeader(XSSFWorkbook workbook, XSSFSheet sheet) {
    var headerStyle = headerStyle(workbook);
    var headerRow = sheet.createRow(0);
    for (var i = 0; i < HEADERS.length; i++) {
      var cell = headerRow.createCell(i);
      cell.setCellValue(HEADERS[i]);
      cell.setCellStyle(headerStyle);
    }
  }

  private void writeRows(XSSFSheet sheet, List<GraduateRow> rows) {
    var rowIndex = 1;
    for (var row : rows) {
      var excelRow = sheet.createRow(rowIndex++);
      excelRow.createCell(0).setCellValue(row.rank());
      excelRow.createCell(1).setCellValue(row.studentNumber());
      excelRow.createCell(2).setCellValue(row.lastName());
      excelRow.createCell(3).setCellValue(row.firstName());
      writeAverageCell(excelRow.createCell(4), row.overallAverage());
    }
  }

  private void writeAverageCell(Cell cell, Double average) {
    if (average == null) {
      cell.setBlank();
      return;
    }
    cell.setCellValue(Math.round(average * 100.0) / 100.0);
  }

  private CellStyle headerStyle(XSSFWorkbook workbook) {
    var font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    var style = workbook.createCellStyle();
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
    style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
    return style;
  }

  private void autoSizeColumns(XSSFSheet sheet) {
    for (var i = 0; i < HEADERS.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }
}
