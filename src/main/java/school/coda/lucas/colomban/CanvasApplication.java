package school.coda.lucas.colomban;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import school.coda.lucas.colomban.controller.GameOverController;
import school.coda.lucas.colomban.modele.Bateau;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.JoueurOrdi;
import school.coda.lucas.colomban.modele.Orientation;
import school.coda.lucas.colomban.modele.TypeBateau;
import school.coda.lucas.colomban.succes.GestionnaireSucces;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CanvasApplication {
    private MediaPlayer lecteurMusiqueJeu;
    private AudioClip sonTouche;
    private AudioClip sonCoule;
    private AudioClip sonRate;

    private boolean enPhaseDePlacement = true;
    private boolean tourDuJoueur = true;
    private JournalDeBord journalDeBord;

    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    /**
     * Marge de la taille d'une case pour y mettre nos lettres et chiffres
     */
    private static final int MARGE = 50;
    /**
     * Position horizontale de la 2ème grille à droite
     */
    private static final int DECALAGE_RADAR = 400;
    private static final int LARGEUR_CANVAS = 800;
    private static final int HAUTEUR_CANVAS = 600;

    private Grille maGrille;
    private JoueurOrdi ordi;
    private SystemeDeTir monSystemeDeTir;

    private int numeroTour = 1;
    private GestionnaireSucces gestionnaireSucces =
            new GestionnaireSucces("Joueur");

    private List<BateauGraphique> flotte;

    private BateauGraphique bateauEnCoursDeDrag = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public void start(Stage stage) {
        journalDeBord = new JournalDeBord();
        journalDeBord.appendText("Placez vos 5 bateaux sur la grille de gauche.");
        URL cheminMusique = getClass().getResource("/school/coda/lucas/colomban/audio/musique_combat.mp3");
        if (cheminMusique != null) {
            Media media = new Media(cheminMusique.toExternalForm());
            lecteurMusiqueJeu = new MediaPlayer(media);
            lecteurMusiqueJeu.setCycleCount(MediaPlayer.INDEFINITE);
            lecteurMusiqueJeu.setVolume(0.4);
            lecteurMusiqueJeu.play();
        } else {
            System.out.println("Musique du jeu introuvable !");
        }

        URL cheminSonTouche = getClass().getResource("/school/coda/lucas/colomban/audio/spas-12.mp3");
        if (cheminSonTouche != null) {
            sonTouche = new AudioClip(cheminSonTouche.toExternalForm());
            sonTouche.setVolume(0.8);
        }

        URL cheminSonCoule = getClass().getResource("/school/coda/lucas/colomban/audio/bruit-coule.mp3");
        if (cheminSonCoule != null) {
            sonCoule = new AudioClip(cheminSonCoule.toExternalForm());
            sonCoule.setVolume(1.0);
        }

        URL cheminSonRate = getClass().getResource("/school/coda/lucas/colomban/audio/bruh.mp3");
        if (cheminSonRate != null) {
            sonRate = new AudioClip(cheminSonRate.toExternalForm());
            sonRate.setVolume(1.0);
        }

        maGrille = new Grille();
        ordi = new JoueurOrdi();

        flotte = new ArrayList<>();
        flotte.add(new BateauGraphique(TypeBateau.PORTE_AVIONS, 50, 420));
        flotte.add(new BateauGraphique(TypeBateau.CUIRASSE, 50, 470));
        flotte.add(new BateauGraphique(TypeBateau.DESTROYER, 50, 520));
        flotte.add(new BateauGraphique(TypeBateau.SOUS_MARIN, 200, 470));
        flotte.add(new BateauGraphique(TypeBateau.PATROUILLEUR, 200, 520));

        final Canvas canvas = new Canvas(LARGEUR_CANVAS, HAUTEUR_CANVAS);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        monSystemeDeTir = new SystemeDeTir(gc);

        canvas.setOnMousePressed(event -> {
            if (!enPhaseDePlacement) return;

            double mx = event.getX();
            double my = event.getY();

            for (int i = flotte.size() - 1; i >= 0; i--) {
                BateauGraphique bateau = flotte.get(i);
                if (bateau.contient(mx, my)) {
                    switch (event.getButton()) {
                        case SECONDARY -> bateau.reorienter(maGrille);
                        case PRIMARY -> {
                            bateau.retirerSiPlaceSur(this.maGrille);
                            bateauEnCoursDeDrag = bateau;
                            dragOffsetX = mx - bateau.x;
                            dragOffsetY = my - bateau.y;
                        }
                    }
                    rafraichirEcran(gc);
                    break;
                }
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (!enPhaseDePlacement) return;
            if (bateauEnCoursDeDrag != null) {
                double x = event.getX() - dragOffsetX;
                double y = event.getY() - dragOffsetY;
                bateauEnCoursDeDrag.moveToPosition(x, y);
                rafraichirEcran(gc);
            }
        });

        canvas.setOnMouseReleased(_ -> {
            if (!enPhaseDePlacement) return;
            if (bateauEnCoursDeDrag != null) {
                bateauEnCoursDeDrag.placerSur(maGrille);
                bateauEnCoursDeDrag = null;
                rafraichirEcran(gc);
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (enPhaseDePlacement) return;
            if (!tourDuJoueur) return;
            if (event.getClickCount() > 1) return;
            if (event.getButton() != MouseButton.PRIMARY) return;

            tirDuJoueur(event, gc);
            if (ordi.estVaincu()) {
                List<String> nouveauxSucces = gestionnaireSucces.validerFinDePartie(true, numeroTour);
                afficherAlertesSucces(nouveauxSucces);

                afficherEcranFin("FÉLICITATIONS !\nVous avez détruit la flotte ennemie !", stage);
                return;
            }

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(_ -> tirDeLOrdi(stage, gc));
            pause.play();

        });

        rafraichirEcran(gc);

        Button btnCombattre = new Button("Combattre");
        btnCombattre.setOnAction(e -> {
            boolean tousPlaces = true;
            for (BateauGraphique b : flotte) {
                if (!b.estPlace) {
                    tousPlaces = false;
                    break;
                }
            }

            if (tousPlaces) {
                enPhaseDePlacement = false;
                ordi.placerBateauxAleatoirement();
                btnCombattre.setText("Bataille en cours...");
                btnCombattre.setDisable(true);
                rafraichirEcran(gc);
            } else {
                btnCombattre.setText("Placez toute la flotte d'abord");
            }
        });

        Group group = new Group();
        group.getChildren().add(canvas);

        VBox conteneur = new VBox(15);
        conteneur.setAlignment(Pos.CENTER);
        conteneur.getChildren().addAll(group, btnCombattre, journalDeBord.getTextArea());

        BorderPane root = new BorderPane(conteneur);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, LARGEUR_CANVAS + 20, HAUTEUR_CANVAS + 160);
        URL cssUrl = getClass().getResource("/school/coda/lucas/colomban/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        root.getStyleClass().add("menu-fond-sot");

        stage.setTitle("Bataille Javale");
        stage.setScene(scene);
        stage.show();
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
    }

    private void tirDuJoueur(MouseEvent event, GraphicsContext gc) {
        double mx = event.getX();
        double my = event.getY();

        boolean horsGrille = !(mx >= DECALAGE_RADAR) || !(mx < DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE)) ||
                             !(my >= MARGE) || !(my < MARGE + (TAILLE_GRILLE * TAILLE_CASE));

        if (horsGrille) {
            return;
        }

        int caseX = (int) ((mx - DECALAGE_RADAR) / TAILLE_CASE);
        int caseY = (int) ((my - MARGE) / TAILLE_CASE);

        if (ordi.isDejaCible(caseY, caseX)) {
            journalDeBord.appendText("ATTENTION : Case " + (char) ('A' + caseY) + "-" + (caseX + 1) + " déjà ciblée ! Tir annulé.");
            return;
        }

        tourDuJoueur = false;
        journalDeBord.appendTour(numeroTour);

        boolean aTouche = ordi.recevoirTir(caseX, caseY);
        String messageTirJoueur = ordi.getDernierMessage();
        journalDeBord.appendTir("VOUS", messageTirJoueur);

        playSoundForAction(messageTirJoueur, aTouche);

        rafraichirEcran(gc);
    }

    private void playSoundForAction(String messageTirJoueur, boolean aTouche) {
        if (messageTirJoueur.contains("Touché-Coulé")) {
            if (sonCoule != null) sonCoule.play();
        } else if (aTouche) {
            if (sonTouche != null) sonTouche.play();
        } else {
            if (sonRate != null) sonRate.play();
        }
    }

    private void tirDeLOrdi(Stage stage, GraphicsContext gc) {
        ordi.jouerTour(maGrille);
        String messageTirOrdi = maGrille.getDernierMessage();
        journalDeBord.appendTir("ORDI", messageTirOrdi);
        journalDeBord.appendBlankLine();

        numeroTour++;

        if (maGrille.estFlotteCoulee()) {

            List<String> nouveauxSucces = gestionnaireSucces.validerFinDePartie(false, numeroTour);
            afficherAlertesSucces(nouveauxSucces);

            afficherEcranFin("DÉFAITE...\nL'ordinateur a coulé tous vos navires.", stage);
            return;
        }

        playSoundForAction(messageTirOrdi, messageTirOrdi.contains("Touché"));

        rafraichirEcran(gc);
        tourDuJoueur = true;
    }

    private void rafraichirEcran(GraphicsContext gc) {
        gc.clearRect(0, 0, LARGEUR_CANVAS, HAUTEUR_CANVAS);

        dessinerDecor(gc);

        gc.setFill(Color.DARKGRAY);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        for (Bateau b : maGrille.getListeBateaux()) {
            double xPixel = MARGE + (b.getCoordonneeX() * TAILLE_CASE);
            double yPixel = MARGE + (b.getCoordonneeY() * TAILLE_CASE);
            double largeur = (b.getOrientation() == Orientation.HORIZONTAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
            double hauteur = (b.getOrientation() == Orientation.VERTICAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
            gc.fillRect(xPixel, yPixel, largeur, hauteur);
            gc.strokeRect(xPixel, yPixel, largeur, hauteur);
        }

        gc.setLineWidth(1);
        for (BateauGraphique b : flotte) {
            if (!b.estPlace) {
                gc.setFill((b == bateauEnCoursDeDrag) ? Color.rgb(100, 100, 100, 0.7) : Color.GRAY);
                double largeur = (b.orientation == Orientation.HORIZONTAL) ? b.type.getTaille() * TAILLE_CASE : TAILLE_CASE;
                double hauteur = (b.orientation == Orientation.VERTICAL) ? b.type.getTaille() * TAILLE_CASE : TAILLE_CASE;
                gc.fillRect(b.x, b.y, largeur, hauteur);
                gc.strokeRect(b.x, b.y, largeur, hauteur);
            }
        }

        monSystemeDeTir.dessinerTousLesTirs(maGrille, ordi.getSaGrille());
    }

    private void dessinerDecor(GraphicsContext gc) {
        gc.setFill(Color.rgb(10, 27, 42, 0.7));
        gc.fillRect(MARGE, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);
        gc.fillRect(DECALAGE_RADAR, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

        if (enPhaseDePlacement) {
            gc.setFill(Color.CYAN);
            gc.fillText("VOTRE FLOTTE (Placez vos bateaux)", MARGE, MARGE - 20);
            gc.setFill(Color.GRAY);
            gc.fillText("RADAR (Désactivé)", DECALAGE_RADAR, MARGE - 20);

            gc.setFill(Color.WHITE);
            gc.fillText("Chantier naval (Clic droit pour tourner): ", 50, 400);
        } else {
            gc.setFill(Color.CYAN);
            gc.fillText("VOTRE FLOTTE (Défense)", MARGE, MARGE - 20);
            gc.setFill(Color.ORANGE);
            gc.fillText("RADAR (Cliquez ici pour attaquer !)", DECALAGE_RADAR, MARGE - 20);
        }

        gc.setFill(Color.WHITE);

        for (int i = 0; i <= TAILLE_GRILLE; i++) {
            double posG1 = MARGE + (i * TAILLE_CASE);
            double posG2 = DECALAGE_RADAR + (i * TAILLE_CASE);

            gc.setStroke(Color.web("#00ffcc"));
            gc.setLineWidth(1.0);

            gc.strokeLine(posG1, MARGE, posG1, MARGE + (TAILLE_GRILLE * TAILLE_CASE));
            gc.strokeLine(MARGE, posG1, MARGE + (TAILLE_GRILLE * TAILLE_CASE), posG1);
            gc.strokeLine(posG2, MARGE, posG2, MARGE + (TAILLE_GRILLE * TAILLE_CASE));
            gc.strokeLine(DECALAGE_RADAR, posG1, DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE), posG1);

            if (i < TAILLE_GRILLE) {
                gc.fillText(String.valueOf(i + 1), posG1 + 10, MARGE - 10);
                gc.fillText(String.valueOf((char) ('A' + i)), MARGE - 20, posG1 + 20);

                gc.fillText(String.valueOf(i + 1), posG2 + 10, MARGE - 10);
                gc.fillText(String.valueOf((char) ('A' + i)), DECALAGE_RADAR - 20, posG1 + 20);
            }
        }
    }

    private void afficherEcranFin(String message, Stage stage) {
        if (lecteurMusiqueJeu != null) {
            lecteurMusiqueJeu.stop();
        }

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

    private static class JournalDeBord {

        private final TextArea textArea;

        public JournalDeBord() {
            textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setPrefHeight(120);
            textArea.setMaxWidth(LARGEUR_CANVAS);

            textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-font-weight: bold;");
        }

        public void appendText(String text) {
            textArea.appendText(">> " + text);
            appendBlankLine();
        }

        public void appendTir(String player, String message) {
            textArea.appendText(player + "  : " + message);
            appendBlankLine();
        }

        public void appendTour(int numeroTour) {
            textArea.appendText("--- TOUR " + numeroTour + " ---");
            appendBlankLine();
        }

        public void appendBlankLine() {
            textArea.appendText("\n");
        }

        /**
         * @return inner javaFx component to be added in JavaFx container
         */
        public TextArea getTextArea() {
            return textArea;
        }
    }

    private static class BateauGraphique {
        TypeBateau type;
        Orientation orientation = Orientation.HORIZONTAL;
        double x, y, startX, startY;
        boolean estPlace = false;

        Bateau bateauLogique = null;

        public BateauGraphique(TypeBateau type, double startX, double startY) {
            this.type = type;
            this.startX = startX;
            this.startY = startY;
            resetToInitialPosition();
        }

        public boolean contient(double mouseX, double mouseY) {
            double largeur = (orientation == Orientation.HORIZONTAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
            double hauteur = (orientation == Orientation.VERTICAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
            return mouseX >= x && mouseX <= x + largeur && mouseY >= y && mouseY <= y + hauteur;
        }

        /**
         * Change l'orientation d'un bateau placé sur la grille (VERTICAL -> HORIZONTAL ou HORIZONTAL -> VERTICAL)
         *
         * <p>Le bateau est retiré de la grille en cas de placement invalide après réorientation
         */
        public void reorienter(Grille grille) {
            orientation = (orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL;

            if (estPlace) {
                grille.retirerBateau(bateauLogique);
                Bateau testPivot = new Bateau(type, orientation, bateauLogique.getCoordonneeX(), bateauLogique.getCoordonneeY());
                if (grille.placerBateau(testPivot)) {
                    bateauLogique = testPivot;
                } else {
                    estPlace = false;
                    bateauLogique = null;
                    resetToInitialPosition();
                }
            }
        }

        public void retirerSiPlaceSur(Grille grille) {
            if (estPlace) {
                grille.retirerBateau(bateauLogique);
                estPlace = false;
                bateauLogique = null;
            }
        }

        public void placerSur(Grille grille) {
            int caseX = (int) ((x + (TAILLE_CASE / 2.0) - MARGE) / TAILLE_CASE);
            int caseY = (int) ((y + (TAILLE_CASE / 2.0) - MARGE) / TAILLE_CASE);

            Bateau bateauTest = new Bateau(type, orientation, caseX, caseY);

            if (grille.placerBateau(bateauTest)) {
                estPlace = true;
                bateauLogique = bateauTest;

                this.x = MARGE + (caseX * TAILLE_CASE);
                this.y = MARGE + (caseY * TAILLE_CASE);
            } else {
                resetToInitialPosition();
            }
        }

        private void resetToInitialPosition() {
            this.x = startX;
            this.y = startY;
            this.orientation = Orientation.HORIZONTAL;
        }

        public void moveToPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}









