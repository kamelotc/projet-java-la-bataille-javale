package school.coda.lucas.colomban.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import school.coda.lucas.colomban.modele.Bateau;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.Orientation;

import java.util.List;

/// Logique de dessin du composant graphique représentant
/// - la grille océan : vaisseux du joueur et tirs reçus
/// - la grille radar : tirs envoyés
/// - le chantier naval : vaisseaux à placer
public class GameBoard {

    private static final int TAILLE_GRILLE = 10;
    public static final int TAILLE_CASE = 30;
    /**
     * Marge de la taille d'une case pour y mettre nos lettres et chiffres
     */
    public static final int MARGE = 50;
    /**
     * Position horizontale de la 2ème grille à droite
     */
    private static final int DECALAGE_RADAR = 350;
    public static final int LARGEUR_CANVAS = 800;
    public static final int HAUTEUR_CANVAS = 600;

    private final Canvas canvas;

    private final GraphicsContext gc;
    private final SystemeDeTir monSystemeDeTir;

    public GameBoard() {
        this.canvas = new Canvas(LARGEUR_CANVAS, HAUTEUR_CANVAS);
        this.gc = canvas.getGraphicsContext2D();
        this.monSystemeDeTir = new SystemeDeTir(gc);

    }

    public Canvas getCanvas() {
        return canvas;
    }

    /// Redessine la zone de jeu
    /// - Grille océan : flotte du joueur et tirs reçus
    /// - Grille radar : tirs envoyés
    ///
    public void rafraichirEcran(ContexteDessinPlateau contexte) {
        gc.clearRect(0, 0, LARGEUR_CANVAS, HAUTEUR_CANVAS);

        dessinerGrilleOcean(contexte);
        dessinerGrilleRadar(contexte);
        dessinerChantierNaval(contexte);
        dessinerTirs(contexte);
    }

    private void dessinerTirs(ContexteDessinPlateau contexte) {
        monSystemeDeTir.dessinerTousLesTirs(contexte.grilleJoueur, contexte.grilleOrdi);
    }

    private void dessinerChantierNaval(ContexteDessinPlateau contexte) {
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

        if (contexte.enPhaseDePlacement) {
            gc.setFill(Color.WHITE);
            gc.fillText("Chantier naval (Clic droit pour tourner): ", 50, 400);
        }

        // Bateaux non placés
        for (BateauGraphique bateau : contexte.flotte) {
            if (bateau.nonPlace()) {
                bateau.dessiner(gc, contexte.bateauEnCoursDeDrag);
            }
        }
    }

    private void dessinerGrilleOcean(ContexteDessinPlateau contexte) {
        int oceanX = 0;
        int oceanY = 0;

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        if (contexte.enPhaseDePlacement) {
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

        for (Bateau b : contexte.grilleJoueur.getListeBateaux()) {
            double xPixel = oceanX + MARGE + (b.getCoordonneeX() * TAILLE_CASE);
            double yPixel = oceanY + MARGE + (b.getCoordonneeY() * TAILLE_CASE);
            double largeur = (b.getOrientation() == Orientation.HORIZONTAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
            double hauteur = (b.getOrientation() == Orientation.VERTICAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
            gc.fillRect(xPixel, yPixel, largeur, hauteur);
            gc.strokeRect(xPixel, yPixel, largeur, hauteur);
        }
    }

    private void dessinerGrilleRadar(ContexteDessinPlateau contexte) {
        int radarX = DECALAGE_RADAR;
        int radarY = 0;

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        // Infos grille radar
        if (contexte.enPhaseDePlacement) {
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

    public static boolean estHorsGrille(double mx, double my) {
        return !(mx >= MARGE + DECALAGE_RADAR) || !(mx < MARGE + DECALAGE_RADAR + (TAILLE_GRILLE * TAILLE_CASE)) || !(my >= MARGE) || !(my < MARGE + (TAILLE_GRILLE * TAILLE_CASE));
    }

    public static int radarYCellFromPixel(double y) {
        return (int) ((y - MARGE) / TAILLE_CASE);
    }

    public static int radarXCellFromPixel(double x) {
        return (int) ((x - (MARGE + DECALAGE_RADAR)) / TAILLE_CASE);
    }

    public static int oceanXCellFromPixel(double x) {
        return (int) ((x + (TAILLE_CASE / 2.0) - MARGE) / TAILLE_CASE);
    }

    public static int oceanYCellFromPixel(double y) {
        return (int) ((y + (TAILLE_CASE / 2.0) - MARGE) / TAILLE_CASE);
    }

    private record SystemeDeTir(GraphicsContext gc) {

        public void dessinerTousLesTirs(Grille grilleJoueur, Grille grilleOrdi) {
            dessinerTirsGrille(grilleJoueur, MARGE);          // À gauche
            dessinerTirsGrille(grilleOrdi, DECALAGE_RADAR + MARGE);   // À droite
        }

        private void dessinerTirsGrille(Grille grille, int decalageX) {
            boolean[][] touches = grille.getTirsTouches();
            boolean[][] rates = grille.getTirsRates();

            for (int y = 0; y < TAILLE_GRILLE; y++) {
                for (int x = 0; x < TAILLE_GRILLE; x++) {
                    double xPixel = decalageX + (x * TAILLE_CASE);
                    double yPixel = MARGE + (y * TAILLE_CASE);

                    if (touches[y][x]) {
                        gc.setFill(Color.RED);
                        gc.fillRect(xPixel + 2, yPixel + 2, TAILLE_CASE - 4, TAILLE_CASE - 4);
                    } else if (rates[y][x]) {
                        gc.setFill(Color.WHITE);
                        gc.fillRect(xPixel + 2, yPixel + 2, TAILLE_CASE - 4, TAILLE_CASE - 4);
                    }
                }
            }
        }
    }

    public record ContexteDessinPlateau(
            Grille grilleJoueur,
            Grille grilleOrdi,
            boolean enPhaseDePlacement,
            List<BateauGraphique> flotte,
            BateauGraphique bateauEnCoursDeDrag) {
    }
}
