package school.coda.lucas.colomban.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import school.coda.lucas.colomban.Main;
import school.coda.lucas.colomban.gui.BateauGraphique;
import school.coda.lucas.colomban.gui.GameBoard;
import school.coda.lucas.colomban.gui.JournalDeBord;
import school.coda.lucas.colomban.gui.LecteurAudio;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.JoueurOrdi;
import school.coda.lucas.colomban.modele.TypeBateau;
import school.coda.lucas.colomban.succes.GestionnaireSucces;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class GameController {

    private final static int COMPUTER_DELAY_MS = 100;

    private final Stage stage;
    private final GestionnaireSucces gestionnaireSucces;
    private final Scene scene;
    private final JournalDeBord journalDeBord;
    private final Grille maGrille;
    private final LecteurAudio lecteur;
    private final JoueurOrdi ordi;
    private final List<BateauGraphique> flotte;
    private final GameBoard gameBoard;

    private boolean enPhaseDePlacement = true;
    private int numeroTour = 1;
    private boolean tourDuJoueur = true;
    private BateauGraphique bateauEnCoursDeDrag = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public GameController(Stage stage) {
        this.stage = stage;

        gameBoard = new GameBoard();
        journalDeBord = new JournalDeBord(GameBoard.LARGEUR_CANVAS);
        journalDeBord.appendText("Placez vos 5 bateaux sur la grille de gauche.");

        lecteur = new LecteurAudio();
        lecteur.startMusicCombat();

        maGrille = new Grille();

        ordi = new JoueurOrdi();

        flotte = new ArrayList<>();
        flotte.add(new BateauGraphique(TypeBateau.PORTE_AVIONS, 50, 420));
        flotte.add(new BateauGraphique(TypeBateau.CUIRASSE, 50, 470));
        flotte.add(new BateauGraphique(TypeBateau.DESTROYER, 50, 520));
        flotte.add(new BateauGraphique(TypeBateau.SOUS_MARIN, 200, 470));
        flotte.add(new BateauGraphique(TypeBateau.PATROUILLEUR, 200, 520));


        this.scene = createScene();
        gestionnaireSucces = new GestionnaireSucces("Joueur");
    }

    private Scene createScene() {

        Canvas canvas = gameBoard.getCanvas();
        setupCanvasEventHandlers(canvas);

        rafraichirEcran();

        Button btnCombattre = createBoutonCombattre();

        Group group = new Group();
        group.getChildren().add(canvas);

        VBox conteneur = new VBox(15);
        conteneur.setAlignment(Pos.CENTER);
        conteneur.getChildren().addAll(group, btnCombattre, journalDeBord.getTextArea());

        BorderPane root = new BorderPane(conteneur);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, GameBoard.LARGEUR_CANVAS + 20, GameBoard.HAUTEUR_CANVAS + 160);
        URL cssUrl = getClass().getResource("/school/coda/lucas/colomban/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        root.getStyleClass().add("menu-fond-sot");
        return scene;
    }


    private void setupCanvasEventHandlers(Canvas canvas) {
        canvas.setOnMousePressed(event -> {
            if (!enPhaseDePlacement) return;
            double mx = event.getX();
            double my = event.getY();

            switch (event.getButton()) {
                case SECONDARY -> orienterBateau(mx, my);
                case PRIMARY -> retirerBateau(mx, my);
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (!enPhaseDePlacement) return;
            onPlacementMouseDragged(event.getX(), event.getY());
        });

        canvas.setOnMouseReleased(_ -> {
            if (!enPhaseDePlacement) return;
            onPlacementMouseReleased();
        });

        canvas.setOnMouseClicked(event -> {
            if (enPhaseDePlacement) return;
            if (!tourDuJoueur) return;
            if (event.getClickCount() > 1) return;
            if (event.getButton() != MouseButton.PRIMARY) return;

            onCombatGrilleRadarClick(event.getX(), event.getY());
        });
    }

    /// - Déclenche le tir sur l'ordinateur.
    /// - Si fin de partie -> redirection sur l'écran Game over
    /// - Contre-attaque de l'ordinateur si la partie n'est pas terminée
    private void onCombatGrilleRadarClick(double mouseX, double mouseY) {
        boolean tirEnvoye = tirDuJoueur(mouseX, mouseY);
        if (!tirEnvoye) {
            return;
        }

        if (ordi.estVaincu()) {
            List<String> nouveauxSucces = gestionnaireSucces.validerFinDePartie(true, numeroTour);
            afficherAlertesSucces(nouveauxSucces);

            afficherEcranFin("FÉLICITATIONS !\nVous avez détruit la flotte ennemie !");
            return;
        }

        PauseTransition pause = new PauseTransition(Duration.millis(COMPUTER_DELAY_MS));
        pause.setOnFinished(_ -> tirDeLOrdi());
        pause.play();
    }

    private void onPlacementMouseReleased() {
        if (bateauEnCoursDeDrag != null) {
            bateauEnCoursDeDrag.placerSur(maGrille);
            bateauEnCoursDeDrag = null;
            rafraichirEcran();
        }
    }

    private void onPlacementMouseDragged(double mouseX, double mouseY) {
        if (bateauEnCoursDeDrag != null) {
            double x = mouseX - dragOffsetX;
            double y = mouseY - dragOffsetY;
            bateauEnCoursDeDrag.moveToPosition(x, y);
            rafraichirEcran();
        }
    }

    private Button createBoutonCombattre() {
        Button btnCombattre = new Button("Combattre");
        btnCombattre.setOnAction(_ -> {
            boolean tousPlaces = true;
            for (BateauGraphique b : flotte) {
                if (b.nonPlace()) {
                    tousPlaces = false;
                    break;
                }
            }

            if (tousPlaces) {
                enPhaseDePlacement = false;
                ordi.placerBateauxAleatoirement();
                btnCombattre.setText("Bataille en cours...");
                btnCombattre.setDisable(true);
                rafraichirEcran();
            } else {
                btnCombattre.setText("Placez toute la flotte d'abord");
            }
        });
        return btnCombattre;
    }

    public Scene getScene() {
        return scene;
    }

    private void retirerBateau(double mx, double my) {
        findBateauEn(mx, my).ifPresent(bateau -> {
            bateau.retirerSiPlaceSur(maGrille);
            bateauEnCoursDeDrag = bateau;
            dragOffsetX = mx - bateau.x();
            dragOffsetY = my - bateau.y();
            rafraichirEcran();
        });
    }

    private void orienterBateau(double mx, double my) {
        findBateauEn(mx, my).ifPresent(bateau -> {
            bateau.reorienter(maGrille);
            rafraichirEcran();
        });
    }

    private Optional<BateauGraphique> findBateauEn(double mx, double my) {
        BateauGraphique bateauPresentAuxCoordonnees = null;
        for (int i = flotte.size() - 1; i >= 0; i--) {
            BateauGraphique bateau = flotte.get(i);
            if (bateau.contient(mx, my)) {
                bateauPresentAuxCoordonnees = bateau;
                break;
            }
        }
        return Optional.ofNullable(bateauPresentAuxCoordonnees);
    }

    private boolean tirDuJoueur(double mx, double my) {

        if (GameBoard.estHorsGrille(mx, my)) {
            return false;
        }

        int caseX = GameBoard.radarXCellFromPixel(mx);
        int caseY = GameBoard.radarYCellFromPixel(my);

        if (ordi.isDejaCible(caseY, caseX)) {
            journalDeBord.appendText("ATTENTION : Case " + (char) ('A' + caseY) + "-" + (caseX + 1) + " déjà ciblée ! Tir annulé.");
            return false;
        }

        tourDuJoueur = false;
        journalDeBord.appendTour(numeroTour);

        boolean aTouche = ordi.recevoirTir(caseX, caseY);
        String messageTirJoueur = ordi.getDernierMessage();
        journalDeBord.appendTir("VOUS", messageTirJoueur);

        jouerSonPourAction(messageTirJoueur, aTouche);

        rafraichirEcran();
        return true;
    }


    private void tirDeLOrdi() {
        ordi.jouerTour(maGrille);
        String messageTirOrdi = maGrille.getDernierMessage();
        journalDeBord.appendTir("ORDI", messageTirOrdi);
        journalDeBord.appendBlankLine();

        numeroTour++;

        if (maGrille.estFlotteCoulee()) {

            List<String> nouveauxSucces = gestionnaireSucces.validerFinDePartie(false, numeroTour);
            afficherAlertesSucces(nouveauxSucces);

            afficherEcranFin("DÉFAITE...\nL'ordinateur a coulé tous vos navires.");
            return;
        }

        jouerSonPourAction(messageTirOrdi, messageTirOrdi.contains("Touché"));

        rafraichirEcran();
        tourDuJoueur = true;
    }

    public void jouerSonPourAction(String messageTirJoueur, boolean aTouche) {
        if (messageTirJoueur.contains("Touché-Coulé")) {
            lecteur.playSonCoule();
        } else if (aTouche) {
            lecteur.playSonTouche();
        } else {
            lecteur.playSonRate();
        }
    }

    private void rafraichirEcran() {
        gameBoard.rafraichirEcran(new GameBoard.ContexteDessinPlateau(
                maGrille, ordi.getSaGrille(), enPhaseDePlacement, flotte, bateauEnCoursDeDrag
        ));
    }

    private void afficherEcranFin(String message) {
        lecteur.stopAllAudio();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("game-over-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 800);

            GameOverController controller = fxmlLoader.getController();
            controller.setWinnerMessage(message);

            stage.setScene(scene);
            stage.setTitle("Fin de la Bataille !");
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'écran de fin : " + e.getMessage());
        }
    }

    private void afficherAlertesSucces(List<String> nouveauxSucces) {
        for (String succes : nouveauxSucces) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès Débloqué !");
            alert.setHeaderText(null);
            alert.setContentText("🏆 Nouveau succès : " + succes + " 🏆");
            alert.showAndWait();
        }
    }

}









