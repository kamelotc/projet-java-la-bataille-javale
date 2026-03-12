package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.media.AudioClip;
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
                System.out.println("Fichier audio introuvable ! Vérifie qu'il est bien dans le dossier resources.");
            }
        }
    }

    @FXML
    protected void onJouerButtonClick(ActionEvent event) {
        if (lecteurMusique != null) {
            lecteurMusique.stop();
            lecteurMusique = null; // On le remet à zéro
        }

        Stage nouvelleFenetre = new Stage();

        nouvelleFenetre.setWidth(850);
        nouvelleFenetre.setHeight(750);
        nouvelleFenetre.setMinWidth(850);
        nouvelleFenetre.setMinHeight(750);
        nouvelleFenetre.setWidth(600);
        nouvelleFenetre.setHeight(600);
        nouvelleFenetre.setMinWidth(600);
        nouvelleFenetre.setMinHeight(600);

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
            stage.setTitle("Bataille Javale - Crédits");

        } catch (IOException e) {
            System.out.println("Impossible de charger la page des crédits");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRetourButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
            Scene sceneMenu = new Scene(fxmlLoader.load(), 400, 400);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(sceneMenu);
            stage.setTitle("Bataille Javale");

        } catch (IOException e) {
            System.out.println("Impossible de charger le menu principal");
            e.printStackTrace();
        }
    }
}