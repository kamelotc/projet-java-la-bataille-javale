package school.coda.lucas.colomban.modele;

import java.util.ArrayList;
import java.util.List;

public class Grille {
    public static final int TAILLE = 10;

    private final Bateau[][] ocean;
    private final List<Bateau> listeBateaux;
    private final boolean[][] tirsRates;
    private final boolean[][] tirsTouches;

    private String dernierMessage = "";

    public Grille() {
        this.ocean = new Bateau[TAILLE][TAILLE];
        this.listeBateaux = new ArrayList<>();
        this.tirsRates = new boolean[TAILLE][TAILLE];
        this.tirsTouches = new boolean[TAILLE][TAILLE];
    }
    public String getDernierMessage() {
        return dernierMessage;
    }

    public boolean placerBateau(Bateau bateau) {
        if (!estPlacementValide(bateau)) {
            System.out.println("Placement impossible pour le " + bateau.getType().getNom());
            return false;
        }

        int taille = bateau.getType().getTaille();
        int x = bateau.getCoordonneeX();
        int y = bateau.getCoordonneeY();

        for (int i = 0; i < taille; i++) {
            if (bateau.getOrientation() == Orientation.HORIZONTAL) {
                ocean[y][x + i] = bateau;
            } else {
                ocean[y + i][x] = bateau;
            }
        }

        listeBateaux.add(bateau);
        return true;
    }


    public void retirerBateau(Bateau bateau) {
        if (bateau != null && listeBateaux.contains(bateau)) {
            listeBateaux.remove(bateau);

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

    private boolean estPlacementValide(Bateau bateau) {
        int taille = bateau.getType().getTaille();
        int startX = bateau.getCoordonneeX();
        int startY = bateau.getCoordonneeY();

        if (startX < 0 || startY < 0 || startX >= TAILLE || startY >= TAILLE) {
            return false;
        }

        if (bateau.getOrientation() == Orientation.HORIZONTAL) {
            if (startX + taille > TAILLE) {
                return false;
            }
        } else {
            if (startY + taille > TAILLE) {
                return false;
            }
        }
        for (int i = 0; i < taille; i++) {
            int caseX = (bateau.getOrientation() == Orientation.HORIZONTAL) ? startX + i : startX;
            int caseY = (bateau.getOrientation() == Orientation.VERTICAL) ? startY + i : startY;

            if (ocean[caseY][caseX] != null) {
                return false;
            }
        }

        return true;
    }

    public List<Bateau> getListeBateaux() {
        return listeBateaux;
    }

    public boolean recevoirTir(int x, int y) {
        if (x < 0 || x >= TAILLE || y < 0 || y >= TAILLE) {
            return false;
        }


        if (tirsRates[y][x] || tirsTouches[y][x]) {
            return false;
        }

        String coordonnees = (char)('A' + y) + "-" + (x + 1);

        if (ocean[y][x] != null) {
            ocean[y][x].toucher();
            tirsTouches[y][x] = true; // TOUCHÉ

            if (ocean[y][x].estCouler()) {

                dernierMessage = coordonnees + " -> Touché-Coulé (" + ocean[y][x].getType().getNom() + ") !";
            } else {
                dernierMessage = coordonnees + " -> Touché !";
            }
            return true;
        } else {
            tirsRates[y][x] = true; // RATÉ
            dernierMessage = coordonnees + " -> Plouf (Raté).";
            return false;
        }
    }

    public boolean estFlotteCoulee() {
        for (Bateau bateau : listeBateaux) {
            if (!bateau.estCouler()) {
                return false;
            }
        }
        return true;
    }

    public boolean[][] getTirsRates() { return tirsRates; }
    public boolean[][] getTirsTouches() { return tirsTouches; }
}