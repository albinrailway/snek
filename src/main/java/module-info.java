module com.example.snek {
    requires javafx.controls;
    requires javafx.fxml;

    // Explicitly allow database and environment variable libraries
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;

    // Open your controllers so JavaFX can link them to the FXML files
    opens com.example.snek.controller to javafx.fxml;
    exports com.example.snek.app;
}