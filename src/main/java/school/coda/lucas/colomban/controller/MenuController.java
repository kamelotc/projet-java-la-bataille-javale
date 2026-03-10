package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import school.coda.lucas.colomban.CanvasApplication;
import school.coda.lucas.colomban.Main;

import java.io.IOException;


public class MenuController {



    @FXML
    private Label welcomeText;

    @FXML
    protected void onJouerButtonClick(ActionEvent event) {
        Stage nouvelleFenetre = new Stage();

        nouvelleFenetre.setWidth(400);
        nouvelleFenetre.setHeight(400);

        nouvelleFenetre.setMinWidth(400);
        nouvelleFenetre.setMinHeight(400);

        CanvasApplication monJeu = new CanvasApplication();
        monJeu.start(nouvelleFenetre);

        Stage menu = (Stage) ((Node) event.getSource()).getScene().getWindow();
        menu.close();
    }
    @FXML
    protected void onAchievementButtonClick() {
    }

    @FXML
    protected void onCreditsButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("credits-view.fxml"));

            Scene sceneCredits = new Scene(fxmlLoader.load(), 400, 400);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(sceneCredits);
            stage.setTitle("Bataille Javale");

        } catch (IOException e) {
            System.out.println("Impossible de charger la page");
            e.printStackTrace();
        }
    }
}

