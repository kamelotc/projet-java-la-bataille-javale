package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import school.coda.lucas.colomban.gui.LecteurAudio;
import school.coda.lucas.colomban.Main;

import java.io.IOException;

public class GameOverController {

    @FXML
    private Label winnerLabel;

    private LecteurAudio lecteurAudio;

    public void initialize() {
        lecteurAudio = new LecteurAudio();
        lecteurAudio.startMusicFin();
    }

    public void setWinnerMessage(String message) {
        winnerLabel.setText(message);

        if (message.contains("FÉLICITATIONS")) {
            lecteurAudio.playSonGagne();
        }
    }

    @FXML
    protected void onRetourMenuClick(ActionEvent event) {
        lecteurAudio.stopAllAudio();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
            Scene sceneMenu = new Scene(fxmlLoader.load(), 800, 800);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(sceneMenu);
            stage.setTitle("Bataille Javale");
            stage.setFullScreen(true); // <-- ET ON FORCE LE PLEIN ÉCRAN ICI AUSSI !
        } catch (IOException e) {
            System.err.println("Erreur lors du retour au menu : " + e.getMessage());
        }
    }
}