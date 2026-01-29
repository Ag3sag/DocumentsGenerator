package com.documentsgenerator;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainController {
    @FXML private Button btnContrato;

    @FXML
    private void initialize() {

        // Configurar acciones de botones del menú principal
        btnContrato.setOnAction(e -> abrirVista("/com/documentsgenerator/ui/CreadorView.fxml", "Administrar Usuarios"));

        //btnLogout.setOnAction(e -> cerrarSesion());

    }

    private void abrirVista(String ruta, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();

            Stage stage = (Stage) btnContrato.getScene().getWindow();

            // ====================== AJUSTE DE TAMAÑO ======================
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Opcional: tamaño inicial deseado
            double initialWidth = 1000;
            double initialHeight = 700;

            Scene scene = new Scene(root,
                    Math.min(initialWidth, screenBounds.getWidth()),
                    Math.min(initialHeight, screenBounds.getHeight()));

            stage.setScene(scene);
            stage.setTitle("DocumentsCreator - " + titulo);

            // Limitar tamaño máximo según pantalla
            stage.setMaxWidth(screenBounds.getWidth());
            stage.setMaxHeight(screenBounds.getHeight());

            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }







}
