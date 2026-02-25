package com.documentsgenerator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
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
    @FXML private TextField txtEmpresaResponsable;
    @FXML private TextField txtNombreResponsable;
    @FXML private TextField txtNitResponsable;
    @FXML private TextField txtCorreoResponsable;
    @FXML private TextField txtCelularArrendatario;
    @FXML private TextField txtCorreoArrendatario;
    @FXML private TextField txtCelularCodeudor;
    @FXML private TextField txtCorreoCodeudor;
    @FXML private Label lblErrorCorreoArrendatario;
    @FXML private Label lblErrorCorreoCodeudor;

    // ====================== COMBOBOX ======================
    @FXML private ComboBox<String> cbDocumentoArrendatario;
    @FXML private ComboBox<String> cbDocumentoCodeudor;

    // ====================== FECHAS ======================
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private DatePicker dpFechaFirma;

    @FXML
    private void initialize() {

        List<String> tiposDocumento = List.of("CC","CE","TI","NIT","PAS","PEP","PPT");
        cbDocumentoArrendatario.getItems().addAll(tiposDocumento);
        cbDocumentoCodeudor.getItems().addAll(tiposDocumento);

        // Solo números
        List<TextField> numeros = List.of(
                txtCedulaArrendatario, txtCedulaCodeudor,
                txtPrecioArriendo, txtPrecioDeposito,
                txtCelularArrendatario, txtCelularCodeudor,
                txtNitResponsable
        );
        numeros.forEach(this::soloNumeros);

        // Validación de correos
        activarValidacionCorreo(txtCorreoArrendatario, lblErrorCorreoArrendatario);
        activarValidacionCorreo(txtCorreoCodeudor, lblErrorCorreoCodeudor);

        // Actualizar precio en letras automáticamente
        txtPrecioArriendo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isBlank()) {
                txtPrecioArriendoLetras.setText("");
            } else {
                try {
                    long valor = Long.parseLong(newVal);
                    txtPrecioArriendoLetras.setText(numeroALetras(valor));
                } catch (NumberFormatException e) {
                    txtPrecioArriendoLetras.setText("");
                }
            }
        });
    }

    // ====================== GENERAR DOCX ======================
    @FXML
    private void onGenerarDocx() {
        if (!validarFormulario()) return;

        try {
            String nombreBase = limpiarNombre("Contrato de Arrendamiento " + txtArrendatario.getText());

            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Selecciona la carpeta de destino");
            File carpeta = dc.showDialog(txtArrendatario.getScene().getWindow());
            if (carpeta == null) return;

            File docxFinal = new File(carpeta, nombreBase + ".docx");

            InputStream template = getClass().getResourceAsStream(
                    "/com/documentsgenerator/templates/CONTRATO_DE_ARRENDAMIENTO.docx"
            );
            if (template == null) {
                showError("No se encontró la plantilla DOCX.");
                return;
            }

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
            String nombreBase = limpiarNombre("Contrato de Arrendamiento " + txtArrendatario.getText());

            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Selecciona la carpeta de destino");
            File carpeta = dc.showDialog(txtArrendatario.getScene().getWindow());
            if (carpeta == null) return;

            File pdfFinal = new File(carpeta, nombreBase + ".pdf");
            File tempDocx = File.createTempFile("temp_", ".docx");

            InputStream template = getClass().getResourceAsStream(
                    "/com/documentsgenerator/templates/CONTRATO_DE_ARRENDAMIENTO.docx"
            );
            if (template == null) {
                showError("No se encontró la plantilla DOCX.");
                return;
            }

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

            File autoPdf = new File(carpeta, tempDocx.getName().replace(".docx", ".pdf"));
            if (autoPdf.exists()) autoPdf.renameTo(pdfFinal);
            tempDocx.delete();

            showInfo("PDF generado correctamente.");

        } catch (Exception e) {
            showError("Error generando PDF: " + e.getMessage());
        }
    }

    // ====================== MAPA DE VALORES ======================
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

        // ===== DURACIÓN =====
        LocalDate inicio = dpFechaInicio.getValue();
        LocalDate fin = dpFechaFin.getValue();
        Period periodo = Period.between(inicio, fin);
        int totalMeses = periodo.getYears() * 12 + periodo.getMonths();

        long valorFinal;
        String unidad;
        if (totalMeses % 12 == 0) {
            valorFinal = totalMeses / 12;
            unidad = (valorFinal == 1) ? "año" : "años";
        } else {
            valorFinal = totalMeses;
            unidad = (valorFinal == 1) ? "mes" : "meses";
        }

        m.put("duracion", String.valueOf(valorFinal));
        m.put("duracionLetras", numeroALetras(valorFinal));
        m.put("mesBool", unidad);

        putFecha(m, inicio, "Inicio");
        putFecha(m, fin, "Fin");
        putFecha(m, dpFechaFirma.getValue(), "");

        return m;
    }

    // ====================== VALIDACIÓN FORMULARIO ======================
    private boolean validarFormulario() {
        if (!validarCamposObligatorios()) return false;
        if (!correoValido(txtCorreoArrendatario.getText())) {
            showError("Correo del arrendatario inválido.");
            return false;
        }
        if (!txtCorreoCodeudor.getText().isBlank() && !correoValido(txtCorreoCodeudor.getText())) {
            showError("Correo del codeudor inválido.");
            return false;
        }
        if (!fechasValidas()) return false;
        return true;
    }

    private boolean fechasValidas() {
        if (dpFechaInicio.getValue() == null || dpFechaFin.getValue() == null || dpFechaFirma.getValue() == null) {
            showError("Debes seleccionar todas las fechas.");
            return false;
        }
        if (dpFechaInicio.getValue().isAfter(dpFechaFin.getValue())) {
            showError("La fecha final no puede ser anterior a la inicial.");
            return false;
        }
        return true;
    }

    // ====================== NÚMERO A LETRAS HASTA MILLONES ======================
    private static final String[] UNIDADES = {"", "Uno", "Dos", "Tres", "Cuatro", "Cinco", "Seis", "Siete", "Ocho", "Nueve"};
    private static final String[] DECENAS = {"", "Diez", "Veinte", "Treinta", "Cuarenta", "Cincuenta", "Sesenta", "Setenta", "Ochenta", "Noventa"};
    private static final String[] ESPECIALES = {"Once","Doce","Trece","Catorce","Quince","Dieciséis","Diecisiete","Dieciocho","Diecinueve"};

    private String numeroALetras(long numero) {
        if (numero == 0) return "Cero";
        if (numero < 0) return "Menos " + numeroALetras(-numero);

        StringBuilder sb = new StringBuilder();

        long millones = numero / 1_000_000;
        numero %= 1_000_000;
        long miles = numero / 1_000;
        long resto = numero % 1_000;

        if (millones > 0) sb.append(convertirMenorMil(millones)).append(" Millón").append(millones > 1 ? "es " : " ");
        if (miles > 0) sb.append(convertirMenorMil(miles)).append(" Mil ");
        if (resto > 0) sb.append(convertirMenorMil(resto));

        return sb.toString().trim();
    }

    private String convertirMenorMil(long num) {
        StringBuilder res = new StringBuilder();

        long centenas = num / 100;
        long decenas = num % 100;

        if (centenas > 0) {
            if (centenas == 1 && decenas == 0) res.append("Cien");
            else res.append(new String[]{"", "Ciento","Doscientos","Trescientos","Cuatrocientos","Quinientos","Seiscientos","Setecientos","Ochocientos","Novecientos"}[(int)centenas]).append(" ");
        }

        if (decenas > 10 && decenas < 20) {
            res.append(ESPECIALES[(int)decenas - 11]);
        } else {
            long d = decenas / 10;
            long u = decenas % 10;
            if (d > 0) res.append(DECENAS[(int)d]).append(u>0 ? " y " : " ");
            if (u>0) res.append(UNIDADES[(int)u]);
        }

        return res.toString().trim();
    }

    // ====================== UTILIDADES ======================
    private void soloNumeros(TextField tf) {
        tf.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*")) tf.setText(n.replaceAll("[^\\d]", ""));
        });
    }

    private boolean correoValido(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void activarValidacionCorreo(TextField campo, Label labelError) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                campo.setStyle(null);
                labelError.setVisible(false);
                return;
            }
            if (!correoValido(newVal)) {
                campo.setStyle("-fx-border-color: red;");
                labelError.setText("Correo inválido");
                labelError.setVisible(true);
            } else {
                campo.setStyle("-fx-border-color: green;");
                labelError.setVisible(false);
            }
        });
    }

    private void putFecha(Map<String, String> m, LocalDate f, String sufijo) {
        m.put("dia" + sufijo, String.valueOf(f.getDayOfMonth()));
        m.put("mes" + sufijo, f.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
        m.put("año" + sufijo, String.valueOf(f.getYear()));
    }

    private String limpiarNombre(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    private void replaceInDocument(XWPFDocument doc, Map<String, String> values) {
        for (XWPFParagraph p : doc.getParagraphs()) replaceInParagraph(p, values);
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
        for (XWPFRun r : runs) if (r.getText(0) != null) sb.append(r.getText(0));
        String text = sb.toString();

        for (var e : values.entrySet())
            text = text.replace("{{" + e.getKey() + "}}", e.getValue());

        for (int i = 0; i < runs.size(); i++)
            runs.get(i).setText(i == runs.size() - 1 ? text : "", 0);
    }

    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }

    private boolean validarCamposObligatorios() {
        Map<TextField, String> campos = new HashMap<>();

        if (txtLugar != null) campos.put(txtLugar, "Lugar");
        if (txtDireccionInmueble != null) campos.put(txtDireccionInmueble, "Dirección del inmueble");
        if (txtArrendatario != null) campos.put(txtArrendatario, "Arrendatario");
        if (txtCedulaArrendatario != null) campos.put(txtCedulaArrendatario, "Cédula del arrendatario");
        if (txtLugarExpedicionArrendatario != null) campos.put(txtLugarExpedicionArrendatario, "Lugar de expedición del arrendatario");
        if (txtPrecioArriendo != null) campos.put(txtPrecioArriendo, "Canon de arrendamiento");
        if (txtPrecioDeposito != null) campos.put(txtPrecioDeposito, "Depósito");
        if (txtEmpresaResponsable != null) campos.put(txtEmpresaResponsable, "Empresa responsable");
        if (txtNombreResponsable != null) campos.put(txtNombreResponsable, "Nombre responsable");
        if (txtNitResponsable != null) campos.put(txtNitResponsable, "NIT responsable");

        List<String> faltantes = new ArrayList<>();
        for (var entry : campos.entrySet()) {
            if (entry.getKey().getText() == null || entry.getKey().getText().isBlank()) {
                faltantes.add(entry.getValue());
                entry.getKey().setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                entry.getKey().setStyle(null);
            }
        }

        if (!faltantes.isEmpty()) {
            showError("Faltan campos obligatorios:\n- " + String.join("\n- ", faltantes));
            return false;
        }

        if (cbDocumentoArrendatario.getValue() == null || cbDocumentoCodeudor.getValue() == null) {
            showError("Selecciona los tipos de documento.");
            return false;
        }

        return true;
    }
}