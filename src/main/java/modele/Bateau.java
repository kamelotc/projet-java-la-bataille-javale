package modele;

public class Bateau {
    private TypeBateau type;
    private Orientation orientation;
    private int coordonneeX;
    private int coordonneeY;
    private int casesTouchees;

    public Bateau(TypeBateau type, Orientation orientation,int coordonneeX, int coordonneeY){
        this.type = type;
        this.orientation = orientation;
        this.coordonneeX = coordonneeX;
        this.coordonneeY = coordonneeY;
        this.casesTouchees = 0;
    }
    public void toucher(){
        this.casesTouchees++;
    }
    public boolean estCouler(){
        return casesTouchees >= type.getTaille();
    }
}
