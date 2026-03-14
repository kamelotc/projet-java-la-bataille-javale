# projet-java-la-bataille-javale
# La Bataille Javale

**La Bataille Javale**, une réinvention du jeu de société de la Bataille Navale, entièrement codée en Java.

## Fonctionnalités Principales

- Interface Graphique JavaFX : Des bateaux via "Drag & Drop".
- Adversaire géré par l'ordinateur qui riposte automatiquement après chacune de vos attaques.
- Musique de fond et bruitages de tirs.
- Sauvegarde automatique (Nombre de parties jouées, victoires, défaites).
- Un tableau de bord pour suivre les accomplissements ("Gagner en moins de 36 tours", "Jouer 50 parties", etc.).

## Architecture du Projet

Le projet a été pensé avec une architecture modulaire et propre, séparant la logique métier de l'affichage :

- **Modèle (`modele`) :** Gestion de la grille, des coordonnées, et de la logique des bateaux (`Bateau`, `Grille`, `TypeBateau`).
- **Interface Graphique (`CanvasApplication`) :** Affichage de la grille interactive utilisant `Canvas` de JavaFX.
- **Contrôleurs (`controller`) :** Les différentes scènes (Menu principal, Écran de fin, Écran des succès).
- **Base de Données (`db`) :** Utilisation de SQLite via l'API JDBC. Les requêtes (`PreparedStatement`) sont centralisées dans une classe dédiée (`StatistiquesDb`).
- **Succès (`succes`) :** Calcul et déblocage des trophées basés sur les données en base.

## Prérequis et Installation

- **JDK 25 .**
- **Maven** (pour la gestion des dépendances comme JavaFX et SQLite).
- Un IDE comme **IntelliJ IDEA**.

### Comment lancer le jeu :

1. Clonez ce dépôt sur votre ordinateur.
2. Ouvrez le projet dans votre IDE (ex: IntelliJ IDEA) en tant que projet Maven.
3. Attendez que Maven télécharge les dépendances (`JavaFX`, `sqlite-jdbc`).
4. Lancez la classe `Main.java` située dans `school.coda.lucas.colomban.Main`.