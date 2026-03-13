package school.coda.lucas.colomban.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StatistiquesDb {

    public void creerTableSiNexistePas() {
        String sqlJoueur = """
                CREATE TABLE IF NOT EXISTS joueur (
                    nom varchar(255) not null primary key,
                    parties_jouees integer default 0,
                    victoires integer default 0
                )""";

        String sqlSucces = """
                CREATE TABLE IF NOT EXISTS joueur_succes (
                    nom_joueur varchar(255) not null,
                    nom_succes varchar(255) not null,
                    PRIMARY KEY (nom_joueur, nom_succes)
                )""";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlJoueur);
            stmt.execute(sqlSucces);
        } catch (SQLException e) {
            System.err.println("Erreur création table : " + e.getMessage());
        }
    }

    public void enregistrerFinDePartie(String nomJoueur, boolean estVictoire) {
        creerJoueurSiNexistePas(nomJoueur);
        String sql = "UPDATE joueur SET parties_jouees = parties_jouees + 1, victoires = victoires + ? WHERE nom = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, estVictoire ? 1 : 0);
            pstmt.setString(2, nomJoueur);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour stats : " + e.getMessage());
        }
    }

    private void creerJoueurSiNexistePas(String nomJoueur) {
        String checkSql = "SELECT nom FROM joueur WHERE nom = ?";
        String insertSql = "INSERT INTO joueur (nom, parties_jouees, victoires) VALUES (?, 0, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, nomJoueur);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, nomJoueur);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur création joueur : " + e.getMessage());
        }
    }

    public int getNombrePartiesJouees(String nomJoueur) {
        String sql = "SELECT parties_jouees FROM joueur WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomJoueur);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("parties_jouees");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture stats : " + e.getMessage());
        }
        return 0;
    }

    public void debloquerSucces(String nomJoueur, String nomSucces) {
        String sql = "INSERT OR IGNORE INTO joueur_succes (nom_joueur, nom_succes) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomJoueur);
            pstmt.setString(2, nomSucces);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde succès : " + e.getMessage());
        }
    }

    public List<String> getSuccesJoueur(String nomJoueur) {
        List<String> succes = new ArrayList<>();
        String sql = "SELECT nom_succes FROM joueur_succes WHERE nom_joueur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomJoueur);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    succes.add(rs.getString("nom_succes"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture succès : " + e.getMessage());
        }
        return succes;
    }
}