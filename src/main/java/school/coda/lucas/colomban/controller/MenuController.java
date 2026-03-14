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
import school.coda.lucas.colomban.CanvasApplication;
import school.coda.lucas.colomban.Main;

import java.io.IOException;
import java.net.URL;

public class MenuController {

    @FXML
    private static MediaPlayer lecteurMusique;
    public Label welcomeText;

    @FXML
    public void initialize() {
        if (lecteurMusique == null) {
            URL cheminMusique = getClass().getResource("/school/coda/lucas/colomban/audio/elden_ring.mp3");

            if (cheminMusique != null) {
                Media media = new Media(cheminMusique.toExternalForm());
                lecteurMusique = new MediaPlayer(media);

                lecteurMusique.setCycleCount(MediaPlayer.INDEFINITE);
                lecteurMusique.play();
            } else {
                System.err.println("Fichier audio introuvable ! Vérifie qu'il est bien dans le dossier resources.");
            }
        }
    }

    @FXML
    protected void onJouerButtonClick(ActionEvent event) {
        if (lecteurMusique != null) {
            lecteurMusique.stop();
            lecteurMusique = null;
        }
        Stage stageActuel = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CanvasApplication monJeu = new CanvasApplication();
        monJeu.start(stageActuel);
    }

    @FXML
    protected void onAchievementButtonClick(ActionEvent event) {
        changerScene(event, "succes-view.fxml", "Bataille Javale - Mes Succès");
    }

    @FXML
    protected void onCreditsButtonClick(ActionEvent event) {
        changerScene(event, "credits-view.fxml", "Bataille Javale - Crédits");
    }

    @FXML
    protected void onRetourButtonClick(ActionEvent event) {
        changerScene(event, "menu-view.fxml", "Bataille Javale");
    }


    private void changerScene(ActionEvent event, String fichierFxml, String titre) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fichierFxml));
            javafx.scene.Parent nouveauDecor = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene sceneActuelle = stage.getScene();
            sceneActuelle.setRoot(nouveauDecor);
            stage.setTitle(titre);

        } catch (IOException e) {
            System.err.println("Impossible de charger la page " + fichierFxml + " : " + e.getMessage());
        }
    }
}