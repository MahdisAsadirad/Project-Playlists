module org.example.demo9 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    opens org.example.demo9.Controller to javafx.fxml;
    opens org.example.demo9.Model.Classes to javafx.fxml;
    opens org.example.demo9.Model.util to javafx.fxml;
    opens org.example.demo9 to javafx.fxml;

    exports org.example.demo9;
    exports org.example.demo9.Controller;
    exports org.example.demo9.Model.Classes;
    exports org.example.demo9.Model.util;
}
