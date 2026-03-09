package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import school.coda.lucas.colomban.Main;


import java.io.IOException;

public class MenuController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onJouerButtonClick(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setTitle("Bataille Javale - En jeu");
        stage.setScene(scene);

    }

    @FXML
    protected void onAchievementButtonClick() {
    }

    @FXML
    protected void onCreditsButtonClick() {
        welcomeText.setText("Créé par Lucas et Colomban");
    }
}

