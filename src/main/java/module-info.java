module school.coda.lucas.colomban {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens school.coda.lucas.colomban to javafx.fxml;
    exports school.coda.lucas.colomban;
    exports school.coda.lucas.colomban.controller;
    opens school.coda.lucas.colomban.controller to javafx.fxml;
}