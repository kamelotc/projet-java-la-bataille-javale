package school.coda.lucas.colomban;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import modele.Bateau;
import modele.Orientation;
import modele.TypeBateau;
import school.coda.lucas.colomban.modele.Grille;


public class CanvasApplication extends Application {

    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    private static final int MARGE = TAILLE_CASE;
    private static final int TAILLE_CANVAS = (TAILLE_GRILLE * TAILLE_CASE) + MARGE;

    @Override
    public void start(Stage stage) {
        final Canvas canvas = new Canvas(TAILLE_CANVAS, TAILLE_CANVAS);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // --- 1. INITIALISATION DU JEU (Test) ---
        Grille maGrille = new Grille();
        // On place un Porte-avions horizontalement en B2 (X=1, Y=1 car on commence à 0)
        maGrille.placerBateau(new Bateau(TypeBateau.PORTE_AVIONS, Orientation.HORIZONTAL, 1, 1));
        // On place un Sous-marin verticalement en E7 (X=6, Y=4)
        maGrille.placerBateau(new Bateau(TypeBateau.SOUS_MARIN, Orientation.VERTICAL, 6, 4));

        // --- 2. DESSIN DU FOND ET DE LA GRILLE ---
        dessinerDecor(gc);

        // --- 3. DESSIN DES BATEAUX ---
        dessinerBateaux(gc, maGrille);

        Group group = new Group();
        group.getChildren().add(canvas);
        BorderPane root = new BorderPane(group);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 400, Color.WHITE);
        stage.setTitle("Bataille Navale - Visualisation");
        stage.setScene(scene);
        stage.show();
    }

    // J'ai séparé le dessin du décor dans une méthode pour que ce soit plus lisible
    private void dessinerDecor(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, TAILLE_CANVAS, TAILLE_CANVAS);

        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MARGE, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.BLACK);

        for (int i = 0; i <= TAILLE_GRILLE; i++) {
            double position = MARGE + (i * TAILLE_CASE);

            gc.setStroke(Color.BLACK);
            gc.strokeLine(position, MARGE, position, TAILLE_CANVAS);
            gc.strokeLine(MARGE, position, TAILLE_CANVAS, position);

            if (i < TAILLE_GRILLE) {
                gc.fillText(String.valueOf(i + 1), position + 10, MARGE - 10);
                gc.fillText(String.valueOf((char) ('A' + i)), MARGE - 20, position + 20);
            }
        }
    }

    // Nouvelle méthode pour dessiner les bateaux
    private void dessinerBateaux(GraphicsContext gc, Grille grille) {
        gc.setFill(Color.DARKGRAY); // Couleur des bateaux

        for (Bateau bateau : grille.getListeBateaux()) {
            // Calcul de la position de départ en pixels (on n'oublie pas la MARGE !)
            double xPixel = MARGE + (bateau.getCoordonneeX() * TAILLE_CASE);
            double yPixel = MARGE + (bateau.getCoordonneeY() * TAILLE_CASE);

            // Calcul de la largeur et hauteur en pixels selon l'orientation
            double largeurPixel;
            double hauteurPixel;

            if (bateau.getOrientation() == Orientation.HORIZONTAL) {
                largeurPixel = bateau.getType().getTaille() * TAILLE_CASE;
                hauteurPixel = TAILLE_CASE;
            } else { // VERTICAL
                largeurPixel = TAILLE_CASE;
                hauteurPixel = bateau.getType().getTaille() * TAILLE_CASE;
            }

            // On dessine le bateau
            gc.fillRect(xPixel, yPixel, largeurPixel, hauteurPixel);

            // Optionnel : on dessine un contour noir autour du bateau pour bien le voir
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeRect(xPixel, yPixel, largeurPixel, hauteurPixel);
            gc.setLineWidth(1); // On remet l'épaisseur normale
        }
    }
}