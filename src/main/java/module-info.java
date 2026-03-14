module school.coda.lucas.colomban {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;



    opens school.coda.lucas.colomban to javafx.fxml;
    exports school.coda.lucas.colomban;
    exports school.coda.lucas.colomban.controller;
    opens school.coda.lucas.colomban.controller to javafx.fxml;
    exports school.coda.lucas.colomban.modele;
    opens school.coda.lucas.colomban.modele to javafx.fxml;
}