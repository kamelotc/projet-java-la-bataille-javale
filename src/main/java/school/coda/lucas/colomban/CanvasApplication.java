package school.coda.lucas.colomban;

import javafx.application.Application;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
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
    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    private static final int MARGE = 30;
    private static final int DECALAGE_RADAR = 400;
    private static final int LARGEUR_CANVAS = 800;
    private static final int HAUTEUR_CANVAS = 600;

    private Grille maGrille;
    private JoueurOrdi ordi;
    private SystemeDeTir monSystemeDeTir;
    private boolean enPhaseDePlacement = true;
    private javafx.scene.control.TextArea journalDeBord;

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

        maGrille = new Grille();
        ordi = new JoueurOrdi();

        flotte = new ArrayList<>();
        flotte.add(new BateauGraphique(TypeBateau.PORTE_AVIONS, 30, 420));
        flotte.add(new BateauGraphique(TypeBateau.CUIRASSE, 30, 470));
        flotte.add(new BateauGraphique(TypeBateau.DESTROYER, 30, 520));
        flotte.add(new BateauGraphique(TypeBateau.SOUS_MARIN, 180, 470));
        flotte.add(new BateauGraphique(TypeBateau.PATROUILLEUR, 180, 520));

        final Canvas canvas = new Canvas(LARGEUR_CANVAS, HAUTEUR_CANVAS);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        monSystemeDeTir = new SystemeDeTir(gc);

        journalDeBord = new javafx.scene.control.TextArea();
        journalDeBord.setEditable(false);
        journalDeBord.setPrefHeight(120);
        journalDeBord.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-font-weight: bold;");
        journalDeBord.appendText(">> Placez vos 5 bateaux sur la grille de gauche.\n");

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

            // 1. SÉCURITÉ ANTI DOUBLE-CLIC : On ignore les clics multiples
            if (event.getClickCount() > 1) return;

            if (event.getButton() == MouseButton.PRIMARY) {
                double mx = event.getX();
                double my = event.getY();

                if (mx >= DECALAGE_RADAR && mx < DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE) &&
                        my >= MARGE && my < MARGE + (TAILLE_GRILLE * TAILLE_CASE)) {

                    int caseX = (int) ((mx - DECALAGE_RADAR) / TAILLE_CASE);
                    int caseY = (int) ((my - MARGE) / TAILLE_CASE);

                    // Anti-triche
                    boolean[][] touches = ordi.getSaGrille().getTirsTouches();
                    boolean[][] rates = ordi.getSaGrille().getTirsRates();

                    if (touches[caseY][caseX] || rates[caseY][caseX]) {
                        journalDeBord.appendText(">> ATTENTION : Case " + (char)('A' + caseY) + "-" + (caseX + 1) + " déjà ciblée ! Tir annulé.\n");
                        return;
                    }

                    ordi.getSaGrille().recevoirTir(caseX, caseY);
                    journalDeBord.appendText("VOUS  : " + ordi.getSaGrille().getDernierMessage() + "\n");

                    if (ordi.getSaGrille().estFlotteCoulee()) {
                        afficherEcranFin("FÉLICITATIONS !\nVous avez détruit la flotte ennemie !", stage);
                        return;
                    }

                    rafraichirEcran(gc); // On affiche tout de suite le tir du joueur

                    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                    pause.setOnFinished(e -> {
                        ordi.jouerTour(maGrille);
                        journalDeBord.appendText("ORDI  : " + maGrille.getDernierMessage() + "\n\n");

                        if (maGrille.estFlotteCoulee()) {
                            afficherEcranFin("DÉFAITE...\nL'ordinateur a coulé tous vos navires.", stage);
                            return;
                        }

                        rafraichirEcran(gc);
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

                // L'ordi place ses bateaux
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

        javafx.scene.layout.VBox conteneur = new javafx.scene.layout.VBox(15);
        conteneur.getChildren().addAll(group, btnCombattre, journalDeBord);

        BorderPane root = new BorderPane(conteneur);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, LARGEUR_CANVAS + 20, HAUTEUR_CANVAS + 160, Color.WHITE);
        stage.setTitle("Bataille Javale");
        stage.setScene(scene);
        stage.show();
    }

    private void rafraichirEcran(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, LARGEUR_CANVAS, HAUTEUR_CANVAS);

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
        // Fond Eau Gauche
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MARGE, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);
        // Fond Eau Droite
        gc.fillRect(DECALAGE_RADAR, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.BLACK);

        for (int i = 0; i <= TAILLE_GRILLE; i++) {
            double posG1 = MARGE + (i * TAILLE_CASE);
            double posG2 = DECALAGE_RADAR + (i * TAILLE_CASE);
            gc.setStroke(Color.BLACK);

            gc.strokeLine(posG1, MARGE, posG1, MARGE + (TAILLE_GRILLE * TAILLE_CASE));
            gc.strokeLine(MARGE, posG1, MARGE + (TAILLE_GRILLE * TAILLE_CASE), posG1);
            // Grille Droite
            gc.strokeLine(posG2, MARGE, posG2, MARGE + (TAILLE_GRILLE * TAILLE_CASE));
            gc.strokeLine(DECALAGE_RADAR, posG1, DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE), posG1);

            if (i < TAILLE_GRILLE) {
                gc.fillText(String.valueOf(i + 1), posG1 + 10, MARGE - 10);
                gc.fillText(String.valueOf((char) ('A' + i)), MARGE - 20, posG1 + 20);
                gc.fillText(String.valueOf(i + 1), posG2 + 10, MARGE - 10);
                gc.fillText(String.valueOf((char) ('A' + i)), DECALAGE_RADAR - 20, posG1 + 20);
            }
        }

        if (enPhaseDePlacement) {
            gc.fillText("VOTRE FLOTTE (Placez vos bateaux)", MARGE, MARGE - 15);
            gc.fillText("RADAR (Désactivé)", DECALAGE_RADAR, MARGE - 15);
            gc.fillText("Chantier naval (Clic droit pour tourner): ", 30, 400);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillText("VOTRE FLOTTE (Défense)", MARGE, MARGE - 15);
            gc.setFill(Color.RED);
            gc.fillText("RADAR (Cliquez ici pour attaquer !)", DECALAGE_RADAR, MARGE - 15);
        }
    }

    private void afficherEcranFin(String message, Stage stage) {

        if (lecteurMusiqueJeu != null) {
            lecteurMusiqueJeu.stop();
        }

        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(school.coda.lucas.colomban.Main.class.getResource("game-over-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 400);

            school.coda.lucas.colomban.controller.GameOverController controller = fxmlLoader.getController();
            controller.setWinnerMessage(message);

            stage.setScene(scene);
            stage.setTitle("Fin de la Bataille !");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}