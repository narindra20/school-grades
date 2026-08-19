package school.hei.students.pdf;

import static java.io.File.createTempFile;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class TranscriptPdfGenerator {

  private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12);
  private static final Font HEADER_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, java.awt.Color.WHITE);
  private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
  private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
  private static final java.awt.Color HEADER_BG = new java.awt.Color(45, 55, 72);

  @SneakyThrows
  public File generate(TranscriptData data) {
    var file = createTempFile("transcript-", ".pdf");
    try (var out = new FileOutputStream(file)) {
      write(data, out, file);
    }
    return file;
  }

  private void write(TranscriptData data, FileOutputStream out, File file)
      throws DocumentException, IOException {
    var document = new Document(PageSize.A4, 50, 50, 50, 50);
    PdfWriter.getInstance(document, out);
    document.open();

    document.add(title());
    document.add(studentHeader(data));
    document.add(spacer());
    document.add(coursesTable(data));
    document.add(spacer());
    document.add(overallAverage(data));
    document.add(footer());

    document.close();
  }

  private Paragraph title() {
    var title = new Paragraph("Relevé de notes officiel", TITLE_FONT);
    title.setAlignment(Element.ALIGN_CENTER);
    title.setSpacingAfter(4f);
    return title;
  }

  private Paragraph studentHeader(TranscriptData data) {
    var header =
        new Paragraph(
            data.studentFirstName()
                + " "
                + data.studentLastName()
                + "  —  N° étudiant : "
                + data.studentNumber()
                + "\nAnnée académique : "
                + data.academicYear(),
            SUBTITLE_FONT);
    header.setAlignment(Element.ALIGN_CENTER);
    return header;
  }

  private Paragraph spacer() {
    return new Paragraph(" ");
  }

  private PdfPTable coursesTable(TranscriptData data) {
    var table = new PdfPTable(new float[] {2f, 5f, 1.5f, 2f});
    table.setWidthPercentage(100);

    addHeaderCell(table, "Code");
    addHeaderCell(table, "Matière");
    addHeaderCell(table, "Crédits");
    addHeaderCell(table, "Moyenne");

    for (var line : data.lines()) {
      addCell(table, line.courseCode(), Element.ALIGN_LEFT);
      addCell(table, line.courseTitle(), Element.ALIGN_LEFT);
      addCell(table, String.valueOf(line.credits()), Element.ALIGN_CENTER);
      addCell(
          table,
          line.average() == null ? "—" : String.format(Locale.FRANCE, "%.2f/20", line.average()),
          Element.ALIGN_CENTER);
    }
    return table;
  }

  private void addHeaderCell(PdfPTable table, String text) {
    var cell = new PdfPCell(new Chunk(text, HEADER_FONT).getImage());
    cell.setBackgroundColor(HEADER_BG);
    cell.setPadding(6f);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(cell);
  }

  private void addCell(PdfPTable table, String text, int alignment) {
    var cell = new PdfPCell(new Chunk(text == null ? "" : text, CELL_FONT).getImage());
    cell.setPadding(5f);
    cell.setHorizontalAlignment(alignment);
    table.addCell(cell);
  }

  private Paragraph overallAverage(TranscriptData data) {
    var text =
        data.overallAverage() == null
            ? "Moyenne générale : —"
            : String.format(Locale.FRANCE, "Moyenne générale : %.2f/20", data.overallAverage());
    var paragraph = new Paragraph(text, TOTAL_FONT);
    paragraph.setAlignment(Element.ALIGN_RIGHT);
    paragraph.setSpacingBefore(10f);
    return paragraph;
  }

  private Paragraph footer() {
    var generatedAt =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneOffset.UTC)
            .format(java.time.Instant.now());
    var paragraph =
        new Paragraph(
            "Document généré automatiquement le " + generatedAt + " (UTC).",
            FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8));
    paragraph.setAlignment(Element.ALIGN_CENTER);
    paragraph.setSpacingBefore(20f);
    return paragraph;
  }
}
