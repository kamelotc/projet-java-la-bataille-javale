package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import school.coda.lucas.colomban.Main;
import school.coda.lucas.colomban.db.StatistiquesDb;
import school.coda.lucas.colomban.succes.GestionnaireSucces;

import java.io.IOException;
import java.util.List;

public class SuccesController {
    @FXML private VBox conteneurSucces;

    @FXML
    public void initialize() {
        StatistiquesDb db = new StatistiquesDb();
        List<String> debloques = db.getSuccesJoueur("Joueur");

        for (String succes : GestionnaireSucces.TOUS_LES_SUCCES) {
            Label label = new Label();

            label.getStyleClass().add("texte-naval");

            if (debloques.contains(succes)) {
                label.setText(succes);
                label.setTextFill(Color.web("#00ffcc"));
            } else {
                label.setText(succes + " (Verrouillé)");
                label.setTextFill(Color.GRAY);
            }
            conteneurSucces.getChildren().add(label);
        }
    }

    @FXML
    protected void onRetourButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
            javafx.scene.Parent nouveauDecor = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene sceneActuelle = stage.getScene();
            sceneActuelle.setRoot(nouveauDecor);
            stage.setTitle("Bataille Javale");

        } catch (IOException e) {
            System.err.println("Impossible de charger le menu principal : " + e.getMessage());
        }
    }
}