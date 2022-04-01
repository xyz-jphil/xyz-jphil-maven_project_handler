package xyz.jphil.maven_project_hander;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    Stage primaryStage;
    public FXMLLoader loader;
    public static VBox pane;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        String fxmlFile = "ProjectHandlerUI.fxml";
        URL fxml = Main.class.getResource(fxmlFile);
        if (fxml == null) {
            System.out.println("Could not load " + fxmlFile + " exiting");
            System.exit(0);
        }
        pane = (VBox) FXMLLoader.load(fxml);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Maven Project Handler");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
