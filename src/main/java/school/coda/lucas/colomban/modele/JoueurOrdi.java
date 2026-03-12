package school.coda.lucas.colomban.modele;

import java.util.Random;

public class JoueurOrdi {
    private Grille saGrille;
    private Random random;

    public JoueurOrdi() {
        this.saGrille = new Grille();
        this.random = new Random();
    }

    // L'ordi place ses 5 bateaux au hasard
    public void placerBateauxAleatoirement() {
        TypeBateau[] types = {
                TypeBateau.PORTE_AVIONS,
                TypeBateau.CUIRASSE,
                TypeBateau.DESTROYER,
                TypeBateau.SOUS_MARIN,
                TypeBateau.PATROUILLEUR
        };

        for (TypeBateau type : types) {
            boolean place = false;
            // Tant que le bateau n'est pas bien placé, on recommence au hasard
            while (!place) {
                int x = random.nextInt(Grille.TAILLE);
                int y = random.nextInt(Grille.TAILLE);
                // Pile ou face pour l'orientation
                Orientation orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;

                Bateau bateauTest = new Bateau(type, orientation, x, y);

                // placerBateau renvoie "true" si la case est libre
                place = saGrille.placerBateau(bateauTest);
            }
        }
        System.out.println("L'ordinateur a placé tous ses bateaux");
    }

    // L'ordi tire au hasard
    public void jouerTour(Grille grilleJoueur) {
        boolean tirValide = false;


        while (!tirValide) {
            int x = random.nextInt(Grille.TAILLE);
            int y = random.nextInt(Grille.TAILLE);

            if (!grilleJoueur.getTirsRates()[y][x] && !grilleJoueur.getTirsTouches()[y][x]) {
                grilleJoueur.recevoirTir(x, y);
                tirValide = true;
            }
        }
    }

    public Grille getSaGrille() {
        return saGrille;
    }
}