package school.coda.lucas.colomban.gui;

import javafx.scene.control.TextArea;

public class JournalDeBord {

    private final TextArea textArea;

    public JournalDeBord(int largeurCanvas) {
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(120);
        textArea.setMaxWidth(largeurCanvas);

        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    public void appendText(String text) {
        textArea.appendText(">> " + text);
        appendBlankLine();
    }

    public void appendTir(String player, String message) {
        textArea.appendText(player + "  : " + message);
        appendBlankLine();
    }

    public void appendTour(int numeroTour) {
        textArea.appendText("--- TOUR " + numeroTour + " ---");
        appendBlankLine();
    }

    public void appendBlankLine() {
        textArea.appendText("\n");
    }

    /**
     * @return inner javaFx component to be added in JavaFx container
     */
    public TextArea getTextArea() {
        return textArea;
    }
}
