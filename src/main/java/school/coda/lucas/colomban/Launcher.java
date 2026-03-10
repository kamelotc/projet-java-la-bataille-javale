package school.coda.lucas.colomban;

import javafx.application.Application;
import school.coda.lucas.colomban.fxml.projetjavalabataillejavale.modele.Bateau;
import school.coda.lucas.colomban.fxml.projetjavalabataillejavale.modele.Orientation;
import school.coda.lucas.colomban.fxml.projetjavalabataillejavale.modele.TypeBateau;
import school.coda.lucas.colomban.fxml.projetjavalabataillejavale.modele.Grille;

public class Launcher {
    public static void main(String[] args) {
        // --- 1. TES TESTS D'ABORD ---
        Grille maGrille = new Grille();
        Bateau b1 = new Bateau(TypeBateau.PORTE_AVIONS, Orientation.HORIZONTAL, 0, 0);
        Bateau b2 = new Bateau(TypeBateau.SOUS_MARIN, Orientation.VERTICAL, 0, 0);

        System.out.println(maGrille.placerBateau(b1)); // Va afficher true
        System.out.println(maGrille.placerBateau(b2)); // Va afficher false (car collision)

        // --- 2. LE LANCEMENT DE LA FENETRE ENSUITE ---
        Application.launch(Main.class, args);
    }
}
