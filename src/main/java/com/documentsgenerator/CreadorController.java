package com.documentsgenerator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class CreadorController {

    // ====================== CAMPOS ======================
    @FXML private TextField txtLugar;
    @FXML private TextField txtDireccionInmueble;
    @FXML private TextField txtArrendatario;
    @FXML private TextField txtCedulaArrendatario;
    @FXML private TextField txtLugarExpedicionArrendatario;
    @FXML private TextField txtCodeudor;
    @FXML private TextField txtCedulaCodeudor;
    @FXML private TextField txtLugarExpedicionCodeudor;
    @FXML private TextField txtPrecioArriendo;
    @FXML private TextField txtPrecioArriendoLetras;
    @FXML private TextField txtPrecioDeposito;
    @FXML private TextField txtDuracionLetras;
    @FXML private TextField txtDuracion;
    @FXML private TextField txtMesBool;
    @FXML private TextField txtEmpresaResponsable;
    @FXML private TextField txtNombreResponsable;
    @FXML private TextField txtNitResponsable;
    @FXML private TextField txtCorreoResponsable;
    @FXML private TextField txtCelularArrendatario;
    @FXML private TextField txtCorreoArrendatario;
    @FXML private TextField txtCelularCodeudor;
    @FXML private TextField txtCorreoCodeudor;

    // ====================== COMBOBOX ======================
    @FXML private ComboBox<String> cbDocumentoArrendatario;
    @FXML private ComboBox<String> cbDocumentoCodeudor;

    // ====================== FECHAS ======================
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private DatePicker dpFechaFirma;

    // ====================== INIT ======================
    @FXML
    private void initialize() {

        List<String> tiposDocumento = List.of(
                "CC",   // Cédula de ciudadanía
                "CE",   // Cédula de extranjería
                "TI",   // Tarjeta de identidad
                "NIT",  // Empresa
                "PAS",  // Pasaporte
                "PEP",  // Permiso especial de permanencia
                "PPT" //Permiso por proteccion temporal
        );

        cbDocumentoArrendatario.getItems().addAll(tiposDocumento);
        cbDocumentoCodeudor.getItems().addAll(tiposDocumento);

        soloNumeros(txtCedulaArrendatario);
        soloNumeros(txtCedulaCodeudor);
        soloNumeros(txtPrecioArriendo);
        soloNumeros(txtPrecioDeposito);
        soloNumeros(txtDuracion);
        soloNumeros(txtCelularArrendatario);
        soloNumeros(txtCelularCodeudor);
        soloNumeros(txtNitResponsable);
    }

    // ====================== GENERAR DOCX ======================
    @FXML
    private void onGenerarDocx() {
        if (!validarFormulario()) return;

        try {
            String nombreBase = limpiarNombre(
                    "Contrato de Arrendamiento " + txtArrendatario.getText()
            );

            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Selecciona la carpeta de destino");
            File carpeta = dc.showDialog(txtArrendatario.getScene().getWindow());
            if (carpeta == null) return;

            File docxFinal = new File(carpeta, nombreBase + ".docx");

            InputStream template = getClass().getResourceAsStream(
                    "/com/documentsgenerator/templates/CONTRATO_DE_ARRENDAMIENTO.docx"
            );

            XWPFDocument doc = new XWPFDocument(template);
            replaceInDocument(doc, buildValuesMap());

            try (FileOutputStream out = new FileOutputStream(docxFinal)) {
                doc.write(out);
            }

            showInfo("DOCX generado correctamente.");

        } catch (Exception e) {
            showError("Error generando DOCX: " + e.getMessage());
        }
    }

    // ====================== GENERAR PDF ======================
    @FXML
    private void onGenerarPdf() {
        if (!validarFormulario()) return;

        try {
            String nombreBase = limpiarNombre(
                    "Contrato de Arrendamiento " + txtArrendatario.getText()
            );

            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Selecciona la carpeta de destino");
            File carpeta = dc.showDialog(txtArrendatario.getScene().getWindow());
            if (carpeta == null) return;

            File pdfFinal = new File(carpeta, nombreBase + ".pdf");
            File tempDocx = File.createTempFile("temp_", ".docx");

            InputStream template = getClass().getResourceAsStream(
                    "/com/documentsgenerator/templates/CONTRATO_DE_ARRENDAMIENTO.docx"
            );

            XWPFDocument doc = new XWPFDocument(template);
            replaceInDocument(doc, buildValuesMap());

            try (FileOutputStream out = new FileOutputStream(tempDocx)) {
                doc.write(out);
            }

            new ProcessBuilder(
                    "soffice", "--headless",
                    "--convert-to", "pdf",
                    "--outdir", carpeta.getAbsolutePath(),
                    tempDocx.getAbsolutePath()
            ).start().waitFor();

            File autoPdf = new File(carpeta,
                    tempDocx.getName().replace(".docx", ".pdf"));

            if (autoPdf.exists()) autoPdf.renameTo(pdfFinal);
            tempDocx.delete();

            showInfo("PDF generado correctamente.");

        } catch (Exception e) {
            showError("Error generando PDF: " + e.getMessage());
        }
    }

    // ====================== MAPA ======================
    private Map<String, String> buildValuesMap() {
        Map<String, String> m = new HashMap<>();

        m.put("lugar", txtLugar.getText());
        m.put("direccionInmueble", txtDireccionInmueble.getText());
        m.put("arrendatario", txtArrendatario.getText());
        m.put("documento", cbDocumentoArrendatario.getValue());
        m.put("cedulaArrendatario", txtCedulaArrendatario.getText());
        m.put("lugarExpedicionArrendatario", txtLugarExpedicionArrendatario.getText());

        m.put("codeudor", txtCodeudor.getText());
        m.put("documentoC", cbDocumentoCodeudor.getValue());
        m.put("cedulaCodeudor", txtCedulaCodeudor.getText());
        m.put("lugarExpedicionCodeudor", txtLugarExpedicionCodeudor.getText());

        m.put("precioArriendo", txtPrecioArriendo.getText());
        m.put("precioArriendoLetras", txtPrecioArriendoLetras.getText());
        m.put("precioDeposito", txtPrecioDeposito.getText());
        m.put("duracion", txtDuracion.getText());
        m.put("duracionLetras", txtDuracionLetras.getText());
        m.put("mesBool", txtMesBool.getText());

        m.put("empresaResponsable", txtEmpresaResponsable.getText());
        m.put("nombreResponsable", txtNombreResponsable.getText());
        m.put("nitResponsable", txtNitResponsable.getText());
        m.put("correoResponsable", txtCorreoResponsable.getText());

        m.put("celularArrendatario", txtCelularArrendatario.getText());
        m.put("correoArrendatario", txtCorreoArrendatario.getText());
        m.put("celularCodeudor", txtCelularCodeudor.getText());
        m.put("correoCodeudor", txtCorreoCodeudor.getText());

        putFecha(m, dpFechaInicio.getValue(), "Inicio");
        putFecha(m, dpFechaFin.getValue(), "Fin");
        putFecha(m, dpFechaFirma.getValue(), "");

        return m;
    }

    // ====================== VALIDACIONES ======================
    private boolean validarFormulario() {

        if (txtArrendatario.getText().isBlank()) {
            showError("El arrendatario es obligatorio.");
            return false;
        }

        if (cbDocumentoArrendatario.getValue() == null) {
            showError("Selecciona el tipo de documento del arrendatario.");
            return false;
        }

        if (!correoValido(txtCorreoArrendatario.getText())) {
            showError("Correo del arrendatario inválido.");
            return false;
        }

        if (!fechasValidas()) return false;

        return true;
    }

    // ====================== UTIL ======================
    private void soloNumeros(TextField tf) {
        tf.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*")) tf.setText(n.replaceAll("[^\\d]", ""));
        });
    }

    private boolean correoValido(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean fechasValidas() {
        if (dpFechaInicio.getValue() == null ||
                dpFechaFin.getValue() == null ||
                dpFechaFirma.getValue() == null) {
            showError("Debes seleccionar todas las fechas.");
            return false;
        }
        return !dpFechaInicio.getValue().isAfter(dpFechaFin.getValue());
    }

    private void putFecha(Map<String, String> m, LocalDate f, String sufijo) {
        m.put("dia" + sufijo, String.valueOf(f.getDayOfMonth()));
        m.put("mes" + sufijo,
                f.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
        m.put("año" + sufijo, String.valueOf(f.getYear()));
    }

    private String limpiarNombre(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    private void replaceInDocument(XWPFDocument doc, Map<String, String> values) {
        for (XWPFParagraph p : doc.getParagraphs())
            replaceInParagraph(p, values);

        for (XWPFTable t : doc.getTables())
            for (XWPFTableRow r : t.getRows())
                for (XWPFTableCell c : r.getTableCells())
                    for (XWPFParagraph p : c.getParagraphs())
                        replaceInParagraph(p, values);
    }

    private void replaceInParagraph(XWPFParagraph p, Map<String, String> values) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null) return;

        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs)
            if (r.getText(0) != null) sb.append(r.getText(0));

        String text = sb.toString();
        for (var e : values.entrySet())
            text = text.replace("{{" + e.getKey() + "}}", e.getValue());

        for (int i = 0; i < runs.size(); i++)
            runs.get(i).setText(i == runs.size() - 1 ? text : "", 0);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}