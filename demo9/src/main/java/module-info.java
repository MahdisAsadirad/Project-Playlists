module org.example.demo9 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.demo9 to javafx.fxml;
    exports org.example.demo9;
}