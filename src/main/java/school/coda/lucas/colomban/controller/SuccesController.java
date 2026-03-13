package school.coda.lucas.colomban.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import school.coda.lucas.colomban.db.StatistiquesDb;
import school.coda.lucas.colomban.succes.GestionnaireSucces;
import java.util.List;

public class SuccesController {
    @FXML private VBox conteneurSucces;

    @FXML
    public void initialize() {
        StatistiquesDb db = new StatistiquesDb();
        List<String> debloques = db.getSuccesJoueur("Joueur");

        for (String succes : GestionnaireSucces.TOUS_LES_SUCCES) {
            Label label = new Label();
            label.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            if (debloques.contains(succes)) {
                label.setText(succes);
                label.setTextFill(Color.GREEN);
            } else {
                label.setText( succes + " (Verrouillé)");
                label.setTextFill(Color.GRAY);
            }
            conteneurSucces.getChildren().add(label);
        }
    }
}