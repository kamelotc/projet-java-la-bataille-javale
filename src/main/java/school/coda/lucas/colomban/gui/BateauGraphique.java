package school.coda.lucas.colomban.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import school.coda.lucas.colomban.modele.Bateau;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.Orientation;
import school.coda.lucas.colomban.modele.TypeBateau;

import static school.coda.lucas.colomban.gui.GameBoard.MARGE;
import static school.coda.lucas.colomban.gui.GameBoard.TAILLE_CASE;

public class BateauGraphique {
    private final TypeBateau type;
    private final double startX;
    private final double startY;
    private double x;
    private double y;
    private boolean estPlace = false;
    private Orientation orientation = Orientation.HORIZONTAL;

    Bateau bateauLogique = null;

    public BateauGraphique(TypeBateau type, double startX, double startY) {
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        resetToInitialPosition();
    }

    public boolean contient(double mouseX, double mouseY) {
        double largeur = (orientation == Orientation.HORIZONTAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
        double hauteur = (orientation == Orientation.VERTICAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
        return mouseX >= x && mouseX <= x + largeur && mouseY >= y && mouseY <= y + hauteur;
    }

    /**
     * Change l'orientation d'un bateau placé sur la grille (VERTICAL -> HORIZONTAL ou HORIZONTAL -> VERTICAL)
     *
     * <p>Le bateau est retiré de la grille en cas de placement invalide après réorientation
     */
    public void reorienter(Grille grille) {
        orientation = (orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL;

        if (estPlace) {
            grille.retirerBateau(bateauLogique);
            Bateau testPivot = new Bateau(type, orientation, bateauLogique.getCoordonneeX(), bateauLogique.getCoordonneeY());
            if (grille.placerBateau(testPivot)) {
                bateauLogique = testPivot;
            } else {
                estPlace = false;
                bateauLogique = null;
                resetToInitialPosition();
            }
        }
    }

    public void retirerSiPlaceSur(Grille grille) {
        if (estPlace) {
            grille.retirerBateau(bateauLogique);
            estPlace = false;
            bateauLogique = null;
        }
    }

    public void placerSur(Grille grille) {
        int caseX = GameBoard.oceanXCellFromPixel(x);
        int caseY = GameBoard.oceanYCellFromPixel(y);
        Bateau bateauTest = new Bateau(type, orientation, caseX, caseY);

        if (grille.placerBateau(bateauTest)) {
            estPlace = true;
            bateauLogique = bateauTest;

            this.x = MARGE + (caseX * TAILLE_CASE);
            this.y = MARGE + (caseY * TAILLE_CASE);
        } else {
            resetToInitialPosition();
        }
    }

    /// Dessine un bateau quand il n'est pas placé
    /// - Dans le chantier
    /// - En cours de drag and drop
    public void dessinerNonPlace(GraphicsContext gc, BateauGraphique bateauEnCoursDeDrag) {
        gc.setLineWidth(1);
        gc.setFill((this == bateauEnCoursDeDrag) ? Color.rgb(100, 100, 100, 0.7) : Color.GRAY);
        double largeur = (orientation == Orientation.HORIZONTAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
        double hauteur = (orientation == Orientation.VERTICAL) ? type.getTaille() * TAILLE_CASE : TAILLE_CASE;
        gc.fillRect(x, y, largeur, hauteur);
        gc.strokeRect(x, y, largeur, hauteur);
    }

    /// Dessine un bateau placé sur une grille
    public static void dessinerPlace(GraphicsContext gc, Bateau b, int grilleOriginX, int grilleOriginY) {

        gc.setFill(Color.DARKGRAY);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        double xPixel = grilleOriginX + MARGE + (b.getCoordonneeX() * TAILLE_CASE);
        double yPixel = grilleOriginY + MARGE + (b.getCoordonneeY() * TAILLE_CASE);

        double largeur = (b.getOrientation() == Orientation.HORIZONTAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;
        double hauteur = (b.getOrientation() == Orientation.VERTICAL) ? b.getType().getTaille() * TAILLE_CASE : TAILLE_CASE;

        gc.fillRect(xPixel, yPixel, largeur, hauteur);
        gc.strokeRect(xPixel, yPixel, largeur, hauteur);
    }

    private void resetToInitialPosition() {
        this.x = startX;
        this.y = startY;
        this.orientation = Orientation.HORIZONTAL;
    }

    public void moveToPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean nonPlace() {
        return !estPlace;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }


}
