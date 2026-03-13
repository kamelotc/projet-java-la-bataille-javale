package school.coda.lucas.colomban;

import javafx.application.Application;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import school.coda.lucas.colomban.modele.Bateau;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.JoueurOrdi;
import school.coda.lucas.colomban.modele.Orientation;
import school.coda.lucas.colomban.modele.TypeBateau;

import java.util.ArrayList;
import java.util.List;

public class CanvasApplication extends Application {
    private MediaPlayer lecteurMusiqueJeu;
    private javafx.scene.media.AudioClip sonTouche;
    private javafx.scene.media.AudioClip sonCoule;
    private javafx.scene.media.AudioClip sonRate;

    private boolean enPhaseDePlacement = true;
    private boolean tourDuJoueur = true;
    private javafx.scene.control.TextArea journalDeBord;

    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    private static final int MARGE = 50;
    private static final int DECALAGE_RADAR = 400;
    private static final int LARGEUR_CANVAS = 800;
    private static final int HAUTEUR_CANVAS = 600;

    private Grille maGrille;
    private JoueurOrdi ordi;
    private SystemeDeTir monSystemeDeTir;

    private int numeroTour = 1;
    private school.coda.lucas.colomban.succes.GestionnaireSucces gestionnaireSucces =
            new school.coda.lucas.colomban.succes.GestionnaireSucces("Joueur");

    private class BateauGraphique {
        TypeBateau type;
        Orientation orientation = Orientation.HORIZONTAL;
        double x, y, startX, startY;
        boolean estPlace = false;

        Bateau bateauLogique = null;

        public BateauGraphique(TypeBateau type, double startX, double startY) {
            this.type = type;
            this.x = startX;
            this.y = startY;
            this.startX = startX;
            this.startY = startY;
        }

        public boolean contient(double mouseX, double mouseY) {
            double largeur = (orientation == Orientation.HORIZONTAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
            double hauteur = (orientation == Orientation.VERTICAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
            return mouseX >= x && mouseX <= x + largeur && mouseY >= y && mouseY <= y + hauteur;
        }
    }

    private List<BateauGraphique> flotte;
    private BateauGraphique bateauEnCoursDeDrag = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    @Override
    public void start(Stage stage) {
        journalDeBord = new javafx.scene.control.TextArea();
        journalDeBord.setEditable(false);
        journalDeBord.setPrefHeight(120);
        journalDeBord.setMaxWidth(LARGEUR_CANVAS);

        journalDeBord.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-font-weight: bold;");
        journalDeBord.appendText(">> Placez vos 5 bateaux sur la grille de gauche.\n");

        java.net.URL cheminMusique = getClass().getResource("/school/coda/lucas/colomban/audio/musique_combat.mp3");
        if (cheminMusique != null) {
            javafx.scene.media.Media media = new javafx.scene.media.Media(cheminMusique.toExternalForm());
            lecteurMusiqueJeu = new javafx.scene.media.MediaPlayer(media);
            lecteurMusiqueJeu.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE);
            lecteurMusiqueJeu.setVolume(0.4);
            lecteurMusiqueJeu.play();
        } else {
            System.out.println("Musique du jeu introuvable !");
        }

        java.net.URL cheminSonTouche = getClass().getResource("/school/coda/lucas/colomban/audio/spas-12.mp3");
        if (cheminSonTouche != null) {
            sonTouche = new javafx.scene.media.AudioClip(cheminSonTouche.toExternalForm());
            sonTouche.setVolume(0.8);
        }

        java.net.URL cheminSonCoule = getClass().getResource("/school/coda/lucas/colomban/audio/bruit-coule.mp3");
        if (cheminSonCoule != null) {
            sonCoule = new javafx.scene.media.AudioClip(cheminSonCoule.toExternalForm());
            sonCoule.setVolume(1.0);
        }

        java.net.URL cheminSonRate = getClass().getResource("/school/coda/lucas/colomban/audio/bruh.mp3");
        if (cheminSonRate != null) {
            sonRate = new javafx.scene.media.AudioClip(cheminSonRate.toExternalForm());
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
                BateauGraphique b = flotte.get(i);

                if (b.contient(mx, my)) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        b.orientation = (b.orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL;

                        if (b.estPlace) {
                            maGrille.retirerBateau(b.bateauLogique);
                            Bateau testPivot = new Bateau(b.type, b.orientation, b.bateauLogique.getCoordonneeX(), b.bateauLogique.getCoordonneeY());
                            if (maGrille.placerBateau(testPivot)) {
                                b.bateauLogique = testPivot;
                            } else {
                                b.estPlace = false;
                                b.bateauLogique = null;
                                b.x = b.startX;
                                b.y = b.startY;
                                b.orientation = Orientation.HORIZONTAL;
                            }
                        }
                    } else if (event.getButton() == MouseButton.PRIMARY) {
                        if (b.estPlace) {
                            maGrille.retirerBateau(b.bateauLogique);
                            b.estPlace = false;
                            b.bateauLogique = null;
                        }
                        bateauEnCoursDeDrag = b;
                        dragOffsetX = mx - b.x;
                        dragOffsetY = my - b.y;
                    }
                    rafraichirEcran(gc);
                    break;
                }
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (!enPhaseDePlacement) return;
            if (bateauEnCoursDeDrag != null) {
                bateauEnCoursDeDrag.x = event.getX() - dragOffsetX;
                bateauEnCoursDeDrag.y = event.getY() - dragOffsetY;
                rafraichirEcran(gc);
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (!enPhaseDePlacement) return;
            if (bateauEnCoursDeDrag != null) {
                int caseX = (int) ((bateauEnCoursDeDrag.x + (TAILLE_CASE / 2) - MARGE) / TAILLE_CASE);
                int caseY = (int) ((bateauEnCoursDeDrag.y + (TAILLE_CASE / 2) - MARGE) / TAILLE_CASE);

                Bateau bateauTest = new Bateau(bateauEnCoursDeDrag.type, bateauEnCoursDeDrag.orientation, caseX, caseY);

                if (maGrille.placerBateau(bateauTest)) {
                    bateauEnCoursDeDrag.estPlace = true;
                    bateauEnCoursDeDrag.bateauLogique = bateauTest;

                    bateauEnCoursDeDrag.x = MARGE + (caseX * TAILLE_CASE);
                    bateauEnCoursDeDrag.y = MARGE + (caseY * TAILLE_CASE);
                } else {
                    bateauEnCoursDeDrag.x = bateauEnCoursDeDrag.startX;
                    bateauEnCoursDeDrag.y = bateauEnCoursDeDrag.startY;
                    bateauEnCoursDeDrag.orientation = Orientation.HORIZONTAL;
                }
                bateauEnCoursDeDrag = null;
                rafraichirEcran(gc);
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (enPhaseDePlacement) return;
            if (!tourDuJoueur) return;
            if (event.getClickCount() > 1) return;

            if (event.getButton() == MouseButton.PRIMARY) {
                double mx = event.getX();
                double my = event.getY();

                if (mx >= DECALAGE_RADAR && mx < DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE) &&
                        my >= MARGE && my < MARGE + (TAILLE_GRILLE * TAILLE_CASE)) {

                    int caseX = (int) ((mx - DECALAGE_RADAR) / TAILLE_CASE);
                    int caseY = (int) ((my - MARGE) / TAILLE_CASE);

                    boolean[][] touches = ordi.getSaGrille().getTirsTouches();
                    boolean[][] rates = ordi.getSaGrille().getTirsRates();

                    if (touches[caseY][caseX] || rates[caseY][caseX]) {
                        journalDeBord.appendText(">> ATTENTION : Case " + (char)('A' + caseY) + "-" + (caseX + 1) + " déjà ciblée ! Tir annulé.\n");
                        return;
                    }

                    tourDuJoueur = false;
                    journalDeBord.appendText("--- TOUR " + numeroTour + " ---\n");

                    boolean aTouche = ordi.getSaGrille().recevoirTir(caseX, caseY);
                    String messageTirJoueur = ordi.getSaGrille().getDernierMessage();
                    journalDeBord.appendText("VOUS  : " + messageTirJoueur + "\n");

                    if (ordi.getSaGrille().estFlotteCoulee()) {

                        // ON VALIDE LES SUCCÈS ICI
                        List<String> nouveauxSucces = gestionnaireSucces.validerFinDePartie(true, numeroTour);

                        for (String succes : nouveauxSucces) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Succès Débloqué !");
                            alert.setHeaderText(null);
                            alert.setContentText("🏆 Nouveau succès : " + succes + " 🏆");
                            alert.showAndWait();
                        }

                        afficherEcranFin("FÉLICITATIONS !\nVous avez détruit la flotte ennemie !", stage);
                        return;
                    } else {
                        if (messageTirJoueur.contains("Touché-Coulé")) {
                            if (sonCoule != null) sonCoule.play();
                        } else if (aTouche) {
                            if (sonTouche != null) sonTouche.play();
                        } else {
                            if (sonRate != null) sonRate.play();
                        }
                    }

                    rafraichirEcran(gc);

                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        ordi.jouerTour(maGrille);
                        String messageTirOrdi = maGrille.getDernierMessage();
                        journalDeBord.appendText("ORDI  : " + messageTirOrdi + "\n\n");

                        numeroTour++;

                        if (maGrille.estFlotteCoulee()) {

                            List<String> nouveauxSucces = gestionnaireSucces.validerFinDePartie(false, numeroTour);

                            for (String succes : nouveauxSucces) {
                                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                alert.setTitle("Succès Débloqué !");
                                alert.setHeaderText(null);
                                alert.setContentText("🏆 Nouveau succès : " + succes + " 🏆");
                                alert.showAndWait();
                            }

                            afficherEcranFin("DÉFAITE...\nL'ordinateur a coulé tous vos navires.", stage);
                            return;
                        } else {
                            if (messageTirOrdi.contains("Touché-Coulé")) {
                                if (sonCoule != null) sonCoule.play();
                            } else if (messageTirOrdi.contains("Touché")) {
                                if (sonTouche != null) sonTouche.play();
                            } else {
                                if (sonRate != null) sonRate.play();
                            }
                        }

                        rafraichirEcran(gc);
                        tourDuJoueur = true;
                    });
                    pause.play();
                }
            }
        });

        rafraichirEcran(gc);

        javafx.scene.control.Button btnCombattre = new javafx.scene.control.Button("Combattre");
        btnCombattre.setOnAction(e -> {
            boolean tousPlaces = true;
            for (BateauGraphique b : flotte) {
                if (!b.estPlace) tousPlaces = false;
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
        conteneur.getChildren().addAll(group, btnCombattre, journalDeBord);

        BorderPane root = new BorderPane(conteneur);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, LARGEUR_CANVAS + 20, HAUTEUR_CANVAS + 160);
        java.net.URL cssUrl = getClass().getResource("/school/coda/lucas/colomban/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        root.getStyleClass().add("menu-fond-sot");

        stage.setTitle("Bataille Javale");
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
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
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(school.coda.lucas.colomban.Main.class.getResource("game-over-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 800);

            school.coda.lucas.colomban.controller.GameOverController controller = fxmlLoader.getController();
            controller.setWinnerMessage(message);

            stage.setScene(scene);
            stage.setTitle("Fin de la Bataille !");
            stage.setFullScreen(true);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

}