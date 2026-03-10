package school.coda.lucas.colomban.modele;

import java.util.ArrayList;
import java.util.List;

public class Grille {
    public static final int TAILLE = 10;
    private Bateau[][] ocean;
    private List<Bateau> listeBateaux;

    public Grille() {
        this.ocean = new Bateau[TAILLE][TAILLE];
        this.listeBateaux = new ArrayList<>();
    }

    /**
     * Tente de placer un bateau sur la grille.
     * @return true si le placement a réussi, false s'il est impossible.
     */
    public boolean placerBateau(Bateau bateau) {
        // 1. On vérifie si le placement est légal
        if (!estPlacementValide(bateau)) {
            System.out.println("Placement impossible pour le " + bateau.getType().getNom());
            return false;
        }

        // 2. Si c'est valide, on place le bateau dans le tableau 2D
        int taille = bateau.getType().getTaille();
        int x = bateau.getCoordonneeX(); // Colonne
        int y = bateau.getCoordonneeY(); // Ligne

        for (int i = 0; i < taille; i++) {
            if (bateau.getOrientation() == Orientation.HORIZONTAL) {
                ocean[y][x + i] = bateau; // On avance sur les colonnes (X)
            } else {
                ocean[y + i][x] = bateau; // On avance sur les lignes (Y)
            }
        }

        // 3. On ajoute le bateau à notre liste pour s'en souvenir
        listeBateaux.add(bateau);
        return true;
    }
    /**
     * Retire un bateau de la grille (pour pouvoir le déplacer).
     */
    public void retirerBateau(Bateau bateau) {
        if (bateau != null && listeBateaux.contains(bateau)) {
            // 1. On le retire de notre liste
            listeBateaux.remove(bateau);

            // 2. On remet les cases de l'océan à null (elles redeviennent de l'eau)
            int taille = bateau.getType().getTaille();
            int x = bateau.getCoordonneeX();
            int y = bateau.getCoordonneeY();

            for (int i = 0; i < taille; i++) {
                if (bateau.getOrientation() == Orientation.HORIZONTAL) {
                    ocean[y][x + i] = null;
                } else {
                    ocean[y + i][x] = null;
                }
            }
        }
    }
    /**
     * Vérifie si le bateau respecte les limites et ne chevauche pas un autre bateau.
     */
    private boolean estPlacementValide(Bateau bateau) {
        int taille = bateau.getType().getTaille();
        int startX = bateau.getCoordonneeX();
        int startY = bateau.getCoordonneeY();

        // 1. Vérification des limites de la grille (0 à 9)
        if (startX < 0 || startY < 0) {
            return false;
        }

        if (bateau.getOrientation() == Orientation.HORIZONTAL) {
            // Si horizontal, on vérifie que le bout du bateau (X + taille) ne dépasse pas 10
            if (startX + taille > TAILLE) {
                return false;
            }
        } else {
            // Si vertical, on vérifie que le bout du bateau (Y + taille) ne dépasse pas 10
            if (startY + taille > TAILLE) {
                return false;
            }
        }

        // 2. Vérification des collisions avec d'autres bateaux
        for (int i = 0; i < taille; i++) {
            int caseX = (bateau.getOrientation() == Orientation.HORIZONTAL) ? startX + i : startX;
            int caseY = (bateau.getOrientation() == Orientation.VERTICAL) ? startY + i : startY;

            // Si la case n'est pas null, c'est qu'il y a déjà un bateau !
            if (ocean[caseY][caseX] != null) {
                return false;
            }
        }


        return true;
    }

    public List<Bateau> getListeBateaux() {
        return listeBateaux;
    }
}