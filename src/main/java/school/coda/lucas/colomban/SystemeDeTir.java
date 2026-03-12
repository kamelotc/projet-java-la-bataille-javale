package school.coda.lucas.colomban;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import school.coda.lucas.colomban.modele.Grille;

public class SystemeDeTir {

    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    private static final int MARGE = 30;
    private static final int DECALAGE_RADAR = 400; // La position de la 2ème grille

    private GraphicsContext gc;

    public SystemeDeTir(GraphicsContext gc) {
        this.gc = gc;
    }

    // On dessine les tirs sur TES bateaux, puis sur la grille de l'ORDI
    public void dessinerTousLesTirs(Grille grilleJoueur, Grille grilleOrdi) {
        dessinerTirsGrille(grilleJoueur, MARGE);          // À gauche
        dessinerTirsGrille(grilleOrdi, DECALAGE_RADAR);   // À droite
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