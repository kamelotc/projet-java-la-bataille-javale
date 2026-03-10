package school.coda.lucas.colomban;

import javafx.application.Application;
import modele.Bateau;
import modele.Orientation;
import modele.TypeBateau;
import school.coda.lucas.colomban.fxml.projetjavalabataillejavale.modele.Grille;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(Main.class, args);
        Grille maGrille = new Grille();
        Bateau b1 = new Bateau(TypeBateau.PORTE_AVIONS, Orientation.HORIZONTAL, 0, 0); // En A1
        Bateau b2 = new Bateau(TypeBateau.SOUS_MARIN, Orientation.VERTICAL, 0, 0);     // En A1 aussi !

        System.out.println(maGrille.placerBateau(b1)); // Va afficher true
        System.out.println(maGrille.placerBateau(b2)); // Va afficher false (car collision en 0,0)
    }
}
