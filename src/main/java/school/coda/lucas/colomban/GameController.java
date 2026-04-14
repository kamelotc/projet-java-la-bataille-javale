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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
import java.util.Optional;

public class GameController {

    private static final int TAILLE_GRILLE = 10;
    public static final int TAILLE_CASE = 30;
    /**
     * Marge de la taille d'une case pour y mettre nos lettres et chiffres
     */
    public static final int MARGE = 50;
    /**
     * Position horizontale de la 2ème grille à droite
     */
    protected static final int DECALAGE_RADAR = 350;
    private static final int DECALAGE_RADAR_AVEC_MARGE = MARGE + DECALAGE_RADAR;
    private static final int LARGEUR_CANVAS = 800;
    private static final int HAUTEUR_CANVAS = 600;
    private static final int COMPUTER_DELAY_MS = 100;

    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final GestionnaireSucces gestionnaireSucces;
    private final Scene scene;
    private final JournalDeBord journalDeBord;
    private final Grille maGrille;
    private final LecteurAudio lecteur;
    private final JoueurOrdi ordi;
    private final SystemeDeTir monSystemeDeTir;
    private final List<BateauGraphique> flotte;

    private int numeroTour = 1;
    private BateauGraphique bateauEnCoursDeDrag = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    private boolean enPhaseDePlacement = true;
    private boolean tourDuJoueur = true;

    public GameController(Stage stage) {
        this.stage = stage;
        canvas = new Canvas(LARGEUR_CANVAS, HAUTEUR_CANVAS);
        gc = canvas.getGraphicsContext2D();

        journalDeBord = new JournalDeBord(LARGEUR_CANVAS);
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

        monSystemeDeTir = new SystemeDeTir(gc);

        this.scene = createScene();
        gestionnaireSucces = new GestionnaireSucces("Joueur");
    }

    private Scene createScene() {

        setupCanvasEventHandlers();

        rafraichirEcran();

        Button btnCombattre = createBoutonCombattre();

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
        return scene;
    }

    private void setupCanvasEventHandlers() {
        canvas.setOnMousePressed(event1 -> {
            if (!enPhaseDePlacement) return;
            double mx = event1.getX();
            double my = event1.getY();

            switch (event1.getButton()) {
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
            dragOffsetX = mx - bateau.x;
            dragOffsetY = my - bateau.y;
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

        boolean horsGrille = !(mx >= MARGE + DECALAGE_RADAR) || !(mx < MARGE + DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE)) || !(my >= MARGE) || !(my < MARGE + (TAILLE_GRILLE * TAILLE_CASE));

        if (horsGrille) {
            return false;
        }

        int caseX = (int) ((mx - (MARGE + DECALAGE_RADAR)) / TAILLE_CASE);
        int caseY = (int) ((my - MARGE) / TAILLE_CASE);

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

    /// Redessine la zone de jeu
    /// - Grille océan : flotte du joueur et tirs reçus
    /// - Grille radar : tirs envoyés
    private void rafraichirEcran() {
        gc.clearRect(0, 0, LARGEUR_CANVAS, HAUTEUR_CANVAS);

        dessinerGrilleOcean();
        dessinerGrilleRadar();
        dessinerChantierNaval();

        monSystemeDeTir.dessinerTousLesTirs(maGrille, ordi.getSaGrille());
    }

    private void dessinerChantierNaval() {
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

        if (enPhaseDePlacement) {
            gc.setFill(Color.WHITE);
            gc.fillText("Chantier naval (Clic droit pour tourner): ", 50, 400);
        }

        // Bateaux non placés
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
    }

    private void dessinerGrilleOcean() {
        int oceanX = 0;
        int oceanY = 0;

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        if (enPhaseDePlacement) {
            gc.setFill(Color.CYAN);
            gc.fillText("VOTRE FLOTTE (Placez vos bateaux)", oceanX + MARGE, oceanY + MARGE - 20);
        } else {
            gc.setFill(Color.CYAN);
            gc.fillText("VOTRE FLOTTE (Défense)", oceanX + MARGE, oceanY + MARGE - 20);
        }

        dessinerDecorGrille(oceanX, oceanY);

        gc.setFill(Color.DARKGRAY);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        for (Bateau b : maGrille.getListeBateaux()) {
            double xPixel = oceanX + MARGE + (b.getCoordonneeX() * TAILLE_CASE);
            double yPixel = oceanY + MARGE + (b.getCoordonneeY() * TAILLE_CASE);
            double largeur = (b.getOrientation() == Orientation.HORIZONTAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
            double hauteur = (b.getOrientation() == Orientation.VERTICAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
            gc.fillRect(xPixel, yPixel, largeur, hauteur);
            gc.strokeRect(xPixel, yPixel, largeur, hauteur);
        }
    }

    private void dessinerGrilleRadar() {
        int radarX = DECALAGE_RADAR;
        int radarY = 0;

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        // Infos grille radar
        if (enPhaseDePlacement) {
            gc.setFill(Color.GRAY);
            gc.fillText("RADAR (Désactivé)", radarX + MARGE, radarY + MARGE - 20);
        } else {
            gc.setFill(Color.ORANGE);
            gc.fillText("RADAR (Cliquez ici pour attaquer !)", radarX + MARGE, radarY + MARGE - 20);
        }

        dessinerDecorGrille(radarX, radarY);
    }

    private void dessinerDecorGrille(int originX, int originY) {

        int width = TAILLE_GRILLE * TAILLE_CASE;
        int height = TAILLE_GRILLE * TAILLE_CASE;

        int x = originX + MARGE;
        int y = originY + MARGE;

        gc.setFill(Color.rgb(10, 27, 42, 0.7));
        gc.fillRect(x, y, width, height);

        gc.setFill(Color.WHITE);

        for (int i = 0; i <= TAILLE_GRILLE; i++) {

            int colX = i * TAILLE_CASE;
            int rowY = i * TAILLE_CASE;

            gc.setStroke(Color.web("#00ffcc"));
            gc.setLineWidth(1.0);

            // Ligne verticale
            gc.strokeLine(x + colX, y, x + colX, y + height);
            // Ligne horizontale
            gc.strokeLine(x, y + rowY, x + width, y + rowY);

            if (i < TAILLE_GRILLE) {
                // Numéros de cases (horizontal)
                gc.fillText(String.valueOf(i + 1), x + colX + 10, y - 10);
                // Lettres de cases (vertical)
                gc.fillText(String.valueOf((char) ('A' + i)), x + -20, y + rowY + 20);
            }
        }
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









