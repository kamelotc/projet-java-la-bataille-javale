package school.coda.lucas.colomban.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import school.coda.lucas.colomban.Main;
import school.coda.lucas.colomban.gui.LecteurAudio;

import java.io.IOException;

public class MenuController {

    private LecteurAudio lecteurAudio;

    public void initialize() {
        lecteurAudio = new LecteurAudio();
        lecteurAudio.startMusicMenu();

    }

    @FXML
    protected void onJouerButtonClick(ActionEvent event) {
        lecteurAudio.stopAllAudio();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        GameController monJeu = new GameController(stage);
        Scene scene = monJeu.getScene();
        stage.setTitle("Bataille Javale");
        stage.setScene(scene);
        stage.show();
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
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