package modele;

public enum TypeBateau {
    PORTE_AVIONS("Porte-avions",5),
    CUIRASSE("Cuirassé",4),
    DESTROYER("Destroyer",3),
    SOUS_MARIN("Sous-marin",3),
    PATROUILLEUR("Patrouilleur",2);

    private final String nom;
    private final int taille;

    TypeBateau (String nom, int taille){
        this.nom = nom;
        this.taille = taille;
    }

    public String getNom(){return nom;}
    public int getTaille(){return taille;}
}
