package school.coda.lucas.colomban.modele;

public class Bateau {
    private final TypeBateau type;
    private final Orientation orientation;
    /**
     * Index de cellule verticalement.
     * Commence à 0
     */
    private final int coordonneeX;
    /**
     * Index de cellule horizontalement.
     * Commence à 0
     */
    private final int coordonneeY;
    private int casesTouchees;

    public Bateau(TypeBateau type, Orientation orientation, int coordonneeX, int coordonneeY) {
        this.type = type;
        this.orientation = orientation;
        this.coordonneeX = coordonneeX;
        this.coordonneeY = coordonneeY;
        this.casesTouchees = 0;
    }

    public void toucher() {
        this.casesTouchees++;
    }

    public boolean estCouler() {
        return casesTouchees >= type.getTaille();
    }

    public TypeBateau getType() {
        return type;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getCoordonneeX() {
        return coordonneeX;
    }

    public int getCoordonneeY() {
        return coordonneeY;
    }
}
