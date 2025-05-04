/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package buatpoints;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Asus
 */
public class BuatPoints extends Application {

    @Override
    public void start(Stage primaryStage) {

        Pane pane = new Pane();

        int[][] points = {
            {2, 2},
            {3, 4},
            {1, 7}
        };

        //menampilkan titik dan label koordinat
        for (int i = 0; i < points.length; i++) {
            int x = points[i][1] * 50;
            int y = points[i][0] * 50;

            Circle circle = new Circle(x, y, 7, Color.ORANGERED);
            circle.setStroke(Color.BLACK);

            Text text = new Text(x + 10, y, "V" + i + " (" + points[i][0] + "," + points[i][1] + ")");
            pane.getChildren().addAll(circle, text);
        }

        Scene scene = new Scene(pane, 500, 500);

        primaryStage.setTitle("Points Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
