package tk.leoforney.phoneimager;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRippler;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by Leo on 2/12/2017.
 */
public class JavaFXMain extends Application {

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hello World!");

        Label label = new Label("TEST");

        JFXButton jfoenixButton = new JFXButton("JFoenix Button");
        JFXButton button = new JFXButton("Raised Button".toUpperCase());
        button.getStyleClass().add("button-raised");

        Pane root = new VBox();
        root.getChildren().add(label);
        root.getChildren().add(jfoenixButton);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();

    }
}
