package school.coda.lucas.colomban;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import school.coda.lucas.colomban.modele.Bateau;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.Orientation;
import school.coda.lucas.colomban.modele.TypeBateau;

import java.util.ArrayList;
import java.util.List;

public class CanvasApplication extends Application {

    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    private static final int MARGE = 30;
    private static final int LARGEUR_CANVAS = 400;
    private static final int HAUTEUR_CANVAS = 600;

    private Grille maGrille;
    private SystemeDeTir monSystemeDeTir;
    private boolean enPhaseDePlacement = true;

    private class BateauGraphique {
        TypeBateau type;
        Orientation orientation = Orientation.HORIZONTAL;
        double x, y, startX, startY;
        boolean estPlace = false;

        // NOUVEAU : On garde un lien vers le "vrai" bateau dans la grille
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
        maGrille = new Grille();
        flotte = new ArrayList<>();
        flotte.add(new BateauGraphique(TypeBateau.PORTE_AVIONS, 30, 380));
        flotte.add(new BateauGraphique(TypeBateau.CUIRASSE, 30, 430));
        flotte.add(new BateauGraphique(TypeBateau.DESTROYER, 30, 480));
        flotte.add(new BateauGraphique(TypeBateau.SOUS_MARIN, 180, 430));
        flotte.add(new BateauGraphique(TypeBateau.PATROUILLEUR, 180, 480));

        final Canvas canvas = new Canvas(LARGEUR_CANVAS, HAUTEUR_CANVAS);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        monSystemeDeTir = new SystemeDeTir(maGrille, gc);

        canvas.setOnMousePressed(event -> {
            if (!enPhaseDePlacement) return;
            double mx = event.getX();
            double my = event.getY();

            // On parcourt à l'envers au cas où des bateaux se superposent au chantier
            for (int i = flotte.size() - 1; i >= 0; i--) {
                BateauGraphique b = flotte.get(i);

                // On a retiré le "!b.estPlace" car on veut cliquer même sur ceux déjà placés !
                if (b.contient(mx, my)) {

                    if (event.getButton() == MouseButton.SECONDARY) { // CLIC DROIT
                        b.orientation = (b.orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL;

                        // Si le bateau était déjà posé, on vérifie s'il peut pivoter sans toucher un autre bateau
                        if (b.estPlace) {
                            maGrille.retirerBateau(b.bateauLogique);
                            Bateau testPivot = new Bateau(b.type, b.orientation, b.bateauLogique.getCoordonneeX(), b.bateauLogique.getCoordonneeY());

                            if (maGrille.placerBateau(testPivot)) {
                                b.bateauLogique = testPivot; // Pivot réussi !
                            } else {
                                // Pas de place pour pivoter, il retourne au port
                                b.estPlace = false;
                                b.bateauLogique = null;
                                b.x = b.startX;
                                b.y = b.startY;
                                b.orientation = Orientation.HORIZONTAL;
                            }
                        }
                    } else if (event.getButton() == MouseButton.PRIMARY) { // CLIC GAUCHE

                        // Si on l'attrape alors qu'il est déjà sur la grille...
                        if (b.estPlace) {
                            maGrille.retirerBateau(b.bateauLogique); // ...on l'arrache de l'eau !
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
                    // NOUVEAU : On sauvegarde le "vrai" bateau créé
                    bateauEnCoursDeDrag.bateauLogique = bateauTest;

                    // BONUS : On "aimante" le bateau pile-poil dans les cases pour un visuel parfait !
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

            if (event.getButton() == MouseButton.PRIMARY) {
                monSystemeDeTir.gererClicTir(event.getX(), event.getY());
                rafraichirEcran(gc);
            }
        });

        rafraichirEcran(gc);

        javafx.scene.control.Button btnCombattre = new javafx.scene.control.Button("Combattre");
        btnCombattre.setOnAction(e -> {
            // On vérifie que tous les bateaux sont posés
            boolean tousPlaces = true;
            for (BateauGraphique b : flotte) {
                if (!b.estPlace) tousPlaces = false;
            }

            if (tousPlaces) {
                enPhaseDePlacement = false;
                btnCombattre.setText("Bataille en cours...");
                btnCombattre.setDisable(true);
            } else {
                btnCombattre.setText("Placez la flotte d'abord");
            }
        });

        Group group = new Group();
        group.getChildren().add(canvas);

        javafx.scene.layout.VBox conteneur = new javafx.scene.layout.VBox(15);
        conteneur.getChildren().addAll(group, btnCombattre);

        BorderPane root = new BorderPane(conteneur);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, LARGEUR_CANVAS + 20, HAUTEUR_CANVAS + 60, Color.WHITE);
        stage.setTitle("Bataille Navale");
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
                if (b == bateauEnCoursDeDrag) {
                    gc.setFill(Color.rgb(100, 100, 100, 0.7));
                } else {
                    gc.setFill(Color.GRAY);
                }

                double largeur = (b.orientation == Orientation.HORIZONTAL) ? b.type.getTaille() * TAILLE_CASE : TAILLE_CASE;
                double hauteur = (b.orientation == Orientation.VERTICAL) ? b.type.getTaille() * TAILLE_CASE : TAILLE_CASE;

                gc.fillRect(b.x, b.y, largeur, hauteur);
                gc.strokeRect(b.x, b.y, largeur, hauteur);
            }
        }
        monSystemeDeTir.dessinerTirs();
    }

    private void dessinerDecor(GraphicsContext gc) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MARGE, MARGE, TAILLE_GRILLE * TAILLE_CASE, TAILLE_GRILLE * TAILLE_CASE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.BLACK);

        for (int i = 0; i <= TAILLE_GRILLE; i++) {
            double position = MARGE + (i * TAILLE_CASE);
            gc.setStroke(Color.BLACK);
            gc.strokeLine(position, MARGE, position, MARGE + (TAILLE_GRILLE * TAILLE_CASE));
            gc.strokeLine(MARGE, position, MARGE + (TAILLE_GRILLE * TAILLE_CASE), position);
            if (i < TAILLE_GRILLE) {
                gc.fillText(String.valueOf(i + 1), position + 10, MARGE - 10);
                gc.fillText(String.valueOf((char) ('A' + i)), MARGE - 20, position + 20);
            }
        }
        gc.fillText("(Clic droit sur le bateau pour le tourner)", 30, 360);
        gc.fillText("Chantier naval: ",30,380);
    }
}