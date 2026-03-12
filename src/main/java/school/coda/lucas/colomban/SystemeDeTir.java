package school.coda.lucas.colomban;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import school.coda.lucas.colomban.modele.Grille;

public class SystemeDeTir {

    // On a besoin de connaître la taille des cases pour faire les calculs
    private static final int TAILLE_GRILLE = 10;
    private static final int TAILLE_CASE = 30;
    private static final int MARGE = 30;

    private Grille grille;
    private GraphicsContext gc;
    public SystemeDeTir(Grille grille, GraphicsContext gc) {
        this.grille = grille;
        this.gc = gc;
    }

    //  Méthode qui s'occupe de calculer le tir
    public void gererClicTir(double mouseX, double mouseY) {
        // On vérifie qu'on clique bien sur la mer
        if (mouseX >= MARGE && mouseY >= MARGE &&
                mouseX < MARGE + (TAILLE_GRILLE * TAILLE_CASE) &&
                mouseY < MARGE + (TAILLE_GRILLE * TAILLE_CASE)) {

            int caseX = (int) ((mouseX - MARGE) / TAILLE_CASE);
            int caseY = (int) ((mouseY - MARGE) / TAILLE_CASE);

            // On envoie le tir à la grille
            grille.recevoirTir(caseX, caseY);
        }
    }

    // 2. La méthode qui dessine les carrés
    public void dessinerTirs() {
        boolean[][] touches = grille.getTirsTouches();
        boolean[][] rates = grille.getTirsRates();

        for (int y = 0; y < TAILLE_GRILLE; y++) {
            for (int x = 0; x < TAILLE_GRILLE; x++) {
                double xPixel = MARGE + (x * TAILLE_CASE);
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