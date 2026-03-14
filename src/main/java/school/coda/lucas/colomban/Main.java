package school.coda.lucas.colomban;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public void start (Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
                stage.setTitle("Bataille Javale");
        stage.setScene(scene);
        stage.show() ;
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
    }

}
