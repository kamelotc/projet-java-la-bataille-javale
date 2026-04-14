package school.coda.lucas.colomban;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.Optional;

class LecteurMusiqueJeu {

    private MediaPlayer musiqueCombat;
    private AudioClip sonTouche;
    private AudioClip sonCoule;
    private AudioClip sonRate;

    public void startMusicCombat() {
        if (musiqueCombat == null) {
            loadMusique("musique_combat.mp3").ifPresent(media -> {
                musiqueCombat = new MediaPlayer(media);
                musiqueCombat.setCycleCount(MediaPlayer.INDEFINITE);
                musiqueCombat.setVolume(0.4);
            });
        }
        musiqueCombat.play();
    }

    public void stopMusicCombat() {
        if (musiqueCombat != null) {
            musiqueCombat.stop();
        }
    }

    public void playSoundForAction(String messageTirJoueur, boolean aTouche) {
        if (messageTirJoueur.contains("Touché-Coulé")) {
            playSonCoule();
        } else if (aTouche) {
            playSonTouche();
        } else {
            playSonRate();
        }
    }

    private void playSonRate() {
        if (sonRate == null) {
            loadSound("bruh.mp3").ifPresent(audioClip -> {
                sonRate = audioClip;
                sonRate.setVolume(1.0);
            });
        }
        playSon(sonRate);
    }

    private void playSonTouche() {
        if (sonTouche == null) {
            loadSound("spas-12.mp3").ifPresent(audioClip -> {
                sonTouche = audioClip;
                sonTouche.setVolume(0.8);
            });
        }
        playSon(sonTouche);
    }

    private void playSonCoule() {
        if (sonCoule == null) {
            loadSound("bruit-coule.mp3").ifPresent(audioClip -> {
                sonCoule = audioClip;
                sonCoule.setVolume(1.0);
            });
        }
        playSon(sonCoule);
    }

    private void playSon(AudioClip son) {
        if (son != null) {
            son.play();
        }
    }

    private Optional<Media> loadMusique(String musicFileName) {
        Optional<Media> media = getAudioFileUrl(musicFileName)
                .map(url -> new Media(url.toExternalForm()));
        if (media.isEmpty()) {
            System.err.println("Musique du jeu introuvable : " + musicFileName);
        }
        return media;
    }

    private Optional<AudioClip> loadSound(String sound) {
        Optional<AudioClip> audioClip = getAudioFileUrl(sound).map(url -> new AudioClip(url.toExternalForm()));
        if (audioClip.isEmpty()) {
            System.err.println("Effet sonore introuvable : " + sound);
        }
        return audioClip;
    }

    private Optional<URL> getAudioFileUrl(String audioFileName) {
        URL resource = LecteurMusiqueJeu.class.getResource("/school/coda/lucas/colomban/audio/" + audioFileName);
        return Optional.ofNullable(resource);
    }
}
