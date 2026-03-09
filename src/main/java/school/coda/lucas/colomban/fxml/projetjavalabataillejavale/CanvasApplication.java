package school.coda.lucas.colomban.fxml.projetjavalabataillejavale;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CanvasApplication extends Application {
    @Override
    public void start(Stage stage) {
        final Canvas canvas = new Canvas(320, 320);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Choix du remplissage
        gc.setFill(Color.BLANCHEDALMOND);
        // Dessiner un rectangle avec le remplissage courant
        // aux coordonnées x:75, y:75
        // de largeur w:303
        // de hauteur h:303
        gc.fillRect(0, 0, 303, 303);

        // Choix de la couleur des traits
        gc.setStroke(Color.BLACK);
        // Dessiner le contour d'un rectangle
        gc.strokeRect(0,0,303,303);

        // Lignes verticales
        gc.strokeLine(101,0,101,303);
        gc.strokeLine(202,0,202,303);

        // Lignes horizontales
        gc.strokeLine(0,101,303,101);
        gc.strokeLine(0,202,303,202);

        Group group = new Group();
        group.getChildren().add(canvas);
        BorderPane root = new BorderPane(group);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 340, 340, Color.WHITE);

        stage.setScene(scene);
        stage.show();
    }
}