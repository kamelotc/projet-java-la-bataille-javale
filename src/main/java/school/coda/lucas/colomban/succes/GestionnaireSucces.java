package school.coda.lucas.colomban.succes;

import school.coda.lucas.colomban.db.StatistiquesDb;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GestionnaireSucces {
    private StatistiquesDb statsDb;
    private String nomDuJoueur;

    public static final List<String> TOUS_LES_SUCCES = Arrays.asList(
            "Gagner une partie",
            "Gagner une partie en moins de 36 tours",
            "Avoir joué 10 parties",
            "Avoir joué 50 parties"
    );

    public GestionnaireSucces(String pseudo) {
        this.nomDuJoueur = pseudo;
        this.statsDb = new StatistiquesDb();
        this.statsDb.creerTableSiNexistePas();
    }

    public List<String> validerFinDePartie(boolean estVictoire, int nombreDeTours) {
        List<String> nouveauxSucces = new ArrayList<>();

        statsDb.enregistrerFinDePartie(nomDuJoueur, estVictoire);

        int totalParties = statsDb.getNombrePartiesJouees(nomDuJoueur);
        List<String> dejaDebloques = statsDb.getSuccesJoueur(nomDuJoueur);

        if (estVictoire) {
            verifierEtDebloquer("Gagner une partie", dejaDebloques, nouveauxSucces);
            if (nombreDeTours < 36) {
                verifierEtDebloquer("Gagner une partie en moins de 36 tours", dejaDebloques, nouveauxSucces);
            }
        }

        if (totalParties >= 10) verifierEtDebloquer("Avoir joué 10 parties", dejaDebloques, nouveauxSucces);
        if (totalParties >= 50) verifierEtDebloquer("Avoir joué 50 parties", dejaDebloques, nouveauxSucces);

        return nouveauxSucces;
    }

    private void verifierEtDebloquer(String nomSucces, List<String> dejaDebloques, List<String> nouveauxSucces) {
        if (!dejaDebloques.contains(nomSucces)) {
            statsDb.debloquerSucces(nomDuJoueur, nomSucces);
            nouveauxSucces.add(nomSucces);
        }
    }
}