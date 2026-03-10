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
    private static final int MARGE = TAILLE_CASE;
    private static final int TAILLE_CANVAS = (TAILLE_GRILLE * TAILLE_CASE) + MARGE;

    @Override
    public void start(Stage stage) {
        final Canvas canvas = new Canvas(TAILLE_CANVAS, TAILLE_CANVAS);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, TAILLE_CANVAS, TAILLE_CANVAS);

        // fond bleu
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MARGE, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);

        // police d'écriture
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.BLACK);

        // boucle pour la grille et le texte
        for (int i = 0; i <= TAILLE_GRILLE; i++) {
            double position = MARGE + (i * TAILLE_CASE);


            gc.setStroke(Color.BLACK);
            // Lignes verticales
            gc.strokeLine(position, MARGE, position, TAILLE_CANVAS);
            // Lignes horizontales
            gc.strokeLine(MARGE, position, TAILLE_CANVAS, position);


            if (i < TAILLE_GRILLE) {
                // Les numéros 1 à 10
                String numero = String.valueOf(i + 1);
                gc.fillText(numero, position + 10, MARGE - 10);

                // Les lettres A à J à gauche
                String lettre = String.valueOf((char) ('A' + i));
                gc.fillText(lettre, MARGE - 20, position + 20);
            }
        }

        Group group = new Group();
        group.getChildren().add(canvas);
        BorderPane root = new BorderPane(group);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 400, 400, Color.WHITE);

        stage.setTitle("Bataille Navale");
        stage.setScene(scene);
        stage.show();
    }
}