package school.coda.lucas.colomban;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.Optional;

class LecteurMusiqueJeu {

    private MediaPlayer lecteurMusiqueJeu;
    private AudioClip sonTouche;
    private AudioClip sonCoule;
    private AudioClip sonRate;

    public LecteurMusiqueJeu() {
        loadMedia("musique_combat.mp3").ifPresent(media -> {
            lecteurMusiqueJeu = new MediaPlayer(media);
            lecteurMusiqueJeu.setCycleCount(MediaPlayer.INDEFINITE);
            lecteurMusiqueJeu.setVolume(0.4);
        });

        loadSound("spas-12.mp3").ifPresent(audioClip -> {
            sonTouche = audioClip;
            sonTouche.setVolume(0.8);
        });

        loadSound("bruit-coule.mp3").ifPresent(audioClip -> {
            sonCoule = audioClip;
            sonCoule.setVolume(1.0);
        });

        loadSound("bruh.mp3").ifPresent(audioClip -> {
            sonRate = audioClip;
            sonRate.setVolume(1.0);
        });
    }

    public void startMusic() {
        if (lecteurMusiqueJeu != null) {
            lecteurMusiqueJeu.play();
        }
    }

    public void stopMusic() {
        if (lecteurMusiqueJeu != null) {
            lecteurMusiqueJeu.stop();
        }
    }

    public void playSoundForAction(String messageTirJoueur, boolean aTouche) {
        if (messageTirJoueur.contains("Touché-Coulé")) {
            playSon(sonCoule);
        } else if (aTouche) {
            playSon(sonTouche);
        } else {
            playSon(sonRate);
        }
    }

    private Optional<AudioClip> loadSound(String sound) {
        Optional<AudioClip> audioClip = getAudioFileUrl(sound).map(url -> new AudioClip(url.toExternalForm()));
        if (audioClip.isEmpty()) {
            System.err.println("Effet sonore introuvable : " + sound);
        }
        return audioClip;
    }

    private void playSon(AudioClip son) {
        if (son != null) son.play();
    }

    private Optional<Media> loadMedia(String music) {
        Optional<Media> media = getAudioFileUrl(music)
                .map(url -> new Media(url.toExternalForm()));
        if (media.isEmpty()) {
            System.err.println("Musique du jeu introuvable : " + music);
        }
        return media;
    }

    private Optional<URL> getAudioFileUrl(String audioFileName) {
        URL resource = LecteurMusiqueJeu.class.getResource("/school/coda/lucas/colomban/audio/" + audioFileName);
        return Optional.ofNullable(resource);
    }
}
