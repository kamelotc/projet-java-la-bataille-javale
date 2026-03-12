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

public class GameOverController {

    @FXML
    private Label winnerLabel;

    public void setWinnerMessage(String message) {
        winnerLabel.setText(message);
    }

    @FXML
    protected void onRetourMenuClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
            Scene sceneMenu = new Scene(fxmlLoader.load(), 400, 400);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(sceneMenu);
            stage.setTitle("Bataille Javale");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}