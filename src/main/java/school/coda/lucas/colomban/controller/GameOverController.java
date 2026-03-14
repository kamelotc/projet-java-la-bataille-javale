package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import school.coda.lucas.colomban.Main;

import java.io.IOException;
import java.net.URL;

public class GameOverController {

    @FXML
    private Label winnerLabel;

    private MediaPlayer lecteurMusiqueFin;
    private javafx.scene.media.AudioClip sonLast;

    @FXML
    public void initialize() {
        URL cheminMusique = getClass().getResource("/school/coda/lucas/colomban/audio/musique_fin.mp3");
        if (cheminMusique != null) {
            Media media = new Media(cheminMusique.toExternalForm());
            lecteurMusiqueFin = new MediaPlayer(media);
            lecteurMusiqueFin.play();
        } else {
            System.out.println("Musique de fin introuvable !");
        }

        URL cheminSonLast = getClass().getResource("/school/coda/lucas/colomban/audio/gagner.mp3");
        if (cheminSonLast != null) {
            sonLast = new javafx.scene.media.AudioClip(cheminSonLast.toExternalForm());
            sonLast.setVolume(1.0);
        } else {
            System.out.println("Son gagner.mp3 introuvable !");
        }
    }

    public void setWinnerMessage(String message) {
        winnerLabel.setText(message);

        if (message.contains("FÉLICITATIONS") && sonLast != null) {
            sonLast.play();
        }
    }

    @FXML
    protected void onRetourMenuClick(ActionEvent event) {
        if (lecteurMusiqueFin != null) {
            lecteurMusiqueFin.stop();
        }
        if (sonLast != null && sonLast.isPlaying()) {
            sonLast.stop();
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
            Scene sceneMenu = new Scene(fxmlLoader.load(), 800, 800);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(sceneMenu);
            stage.setTitle("Bataille Javale");
            stage.setFullScreen(true); // <-- ET ON FORCE LE PLEIN ÉCRAN ICI AUSSI !
        } catch (IOException e) {
            System.err.println("Erreur lors du retour au menu : " + e.getMessage());        }
    }
}