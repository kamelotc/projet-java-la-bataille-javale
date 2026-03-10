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

public class CanvasApplication extends Application {

    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    // On crée une marge de la taille d'une case pour y mettre nos lettres et chiffres
    private static final int MARGE = TAILLE_CASE;
    private static final int TAILLE_CANVAS = (TAILLE_GRILLE * TAILLE_CASE) + MARGE;

    @Override
    public void start(Stage stage) {
        final Canvas canvas = new Canvas(TAILLE_CANVAS, TAILLE_CANVAS);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. On remplit le fond de l'espace des marges en blanc
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, TAILLE_CANVAS, TAILLE_CANVAS);

        // 2. On dessine le fond bleu de l'eau, mais décalé par la MARGE
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MARGE, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);

        // Configuration de la police d'écriture
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.BLACK);

        // 3. La boucle magique pour la grille ET le texte
        for (int i = 0; i <= TAILLE_GRILLE; i++) {
            double position = MARGE + (i * TAILLE_CASE);

            // --- DESSIN DES LIGNES ---
            gc.setStroke(Color.BLACK);
            // Lignes verticales
            gc.strokeLine(position, MARGE, position, TAILLE_CANVAS);
            // Lignes horizontales
            gc.strokeLine(MARGE, position, TAILLE_CANVAS, position);

            // --- DESSIN DU TEXTE (seulement pour i de 0 à 9) ---
            if (i < TAILLE_GRILLE) {
                // Les numéros 1 à 10 en haut
                String numero = String.valueOf(i + 1);
                // On fait +10 pour centrer un peu le chiffre au milieu de sa colonne
                gc.fillText(numero, position + 10, MARGE - 10);

                // Les lettres A à J à gauche
                // L'astuce : 'A' vaut 65 en informatique. 'A' + 1 = 66 = 'B', etc.
                String lettre = String.valueOf((char) ('A' + i));
                // On fait +20 pour centrer un peu la lettre au milieu de sa ligne
                gc.fillText(lettre, MARGE - 20, position + 20);
            }
        }

        Group group = new Group();
        group.getChildren().add(canvas);
        BorderPane root = new BorderPane(group);
        root.setPadding(new Insets(20));

        // La scène est un peu plus grande pour tout contenir
        Scene scene = new Scene(root, 400, 400, Color.WHITE);

        stage.setTitle("Bataille Navale");
        stage.setScene(scene);
        stage.show();
    }
}