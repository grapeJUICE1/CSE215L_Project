module com.example.newsfeedmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.newsfeedmanagementsystem to javafx.fxml;
    exports com.example.newsfeedmanagementsystem;
}