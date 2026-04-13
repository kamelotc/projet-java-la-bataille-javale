package school.coda.lucas.colomban;

import school.coda.lucas.colomban.modele.Bateau;
import school.coda.lucas.colomban.modele.Grille;
import school.coda.lucas.colomban.modele.Orientation;
import school.coda.lucas.colomban.modele.TypeBateau;

class BateauGraphique {
    TypeBateau type;
    Orientation orientation = Orientation.HORIZONTAL;
    double x, y, startX, startY;
    boolean estPlace = false;

    Bateau bateauLogique = null;

    public BateauGraphique(TypeBateau type, double startX, double startY) {
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        resetToInitialPosition();
    }

    public boolean contient(double mouseX, double mouseY) {
        double largeur = (orientation == Orientation.HORIZONTAL) ? type.getTaille() * GameController.TAILLE_CASE : GameController.TAILLE_CASE;
        double hauteur = (orientation == Orientation.VERTICAL) ? type.getTaille() * GameController.TAILLE_CASE : GameController.TAILLE_CASE;
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
        int caseX = (int) ((x + (GameController.TAILLE_CASE / 2.0) - GameController.MARGE) / GameController.TAILLE_CASE);
        int caseY = (int) ((y + (GameController.TAILLE_CASE / 2.0) - GameController.MARGE) / GameController.TAILLE_CASE);

        Bateau bateauTest = new Bateau(type, orientation, caseX, caseY);

        if (grille.placerBateau(bateauTest)) {
            estPlace = true;
            bateauLogique = bateauTest;

            this.x = GameController.MARGE + (caseX * GameController.TAILLE_CASE);
            this.y = GameController.MARGE + (caseY * GameController.TAILLE_CASE);
        } else {
            resetToInitialPosition();
        }
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
}
