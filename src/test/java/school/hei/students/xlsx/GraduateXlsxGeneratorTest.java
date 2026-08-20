package school.hei.students.xlsx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

class GraduateXlsxGeneratorTest {
  private final GraduateXlsxGenerator generator = new GraduateXlsxGenerator();

  @Test
  void generates_header_and_rows() throws Exception {
    var rows =
        List.of(
            GraduateRow.builder()
                .rank(1)
                .studentNumber("25001")
                .lastName("Rakoto")
                .firstName("Fitia")
                .overallAverage(16.5)
                .build(),
            GraduateRow.builder()
                .rank(2)
                .studentNumber("25002")
                .lastName("Rabe")
                .firstName("Hery")
                .overallAverage(14.2)
                .build());

    var file = generator.generate(rows);

    try (var workbook = WorkbookFactory.create(file)) {
      var sheet = workbook.getSheetAt(0);
      var header = sheet.getRow(0);
      assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Rang");
      assertThat(header.getCell(1).getStringCellValue()).isEqualTo("STD");
      assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Nom");
      assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Prénom");
      assertThat(header.getCell(4).getStringCellValue()).isEqualTo("Moyenne générale");

      var firstDataRow = sheet.getRow(1);
      assertThat(firstDataRow.getCell(0).getNumericCellValue()).isEqualTo(1.0);
      assertThat(firstDataRow.getCell(1).getStringCellValue()).isEqualTo("25001");
      assertThat(firstDataRow.getCell(2).getStringCellValue()).isEqualTo("Rakoto");
      assertThat(firstDataRow.getCell(3).getStringCellValue()).isEqualTo("Fitia");
      assertThat(firstDataRow.getCell(4).getNumericCellValue()).isEqualTo(16.5);

      var secondDataRow = sheet.getRow(2);
      assertThat(secondDataRow.getCell(1).getStringCellValue()).isEqualTo("25002");
    }
    file.delete();
  }

  @Test
  void handles_empty_list() throws Exception {
    var file = generator.generate(List.of());
    try (var workbook = WorkbookFactory.create(file)) {
      var sheet = workbook.getSheetAt(0);
      assertThat(sheet.getLastRowNum()).isZero();
    }
    file.delete();
  }

  @Test
  void writes_blank_cell_when_average_is_null() throws Exception {
    var rows =
        List.of(
            GraduateRow.builder()
                .rank(1)
                .studentNumber("25001")
                .lastName("Rakoto")
                .firstName("Fitia")
                .overallAverage(null)
                .build());
    var file = generator.generate(rows);
    try (var workbook = WorkbookFactory.create(file)) {
      var sheet = workbook.getSheetAt(0);
      var cell = sheet.getRow(1).getCell(4);
      assertThat(cell.getCellType().toString()).isEqualTo("BLANK");
    }
    file.delete();
  }
}
