/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koordinatCartesian;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Asus
 */
public class KoordinatCartesian extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        int width = 400;
        int height = 400;
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        int centerX = width / 2;
        int centerY = height / 2;

        //gambar sumbu X dan Y
        gc.strokeLine(0, centerY, width, centerY);
        gc.strokeLine(centerX, 0, centerX, height);

        int scale = 30; 
        gc.setLineWidth(1);
        gc.setStroke(Color.GREY);

        // Titik (2,3) - Hijau
        double px1 = centerX + 2 * scale;
        double py1 = centerY - 3 * scale;
        gc.setFill(Color.GREEN);
        gc.fillOval(px1 - 3, py1 - 3, 6, 6);
        gc.strokeText("(2,3)", px1 + 5, py1 - 5);

        // Titik (-3,1) - Merah
        double px2 = centerX + (-3) * scale;
        double py2 = centerY - 1 * scale;
        gc.setFill(Color.RED);
        gc.fillOval(px2 - 3, py2 - 3, 6, 6);
        gc.strokeText("(-3,1)", px2 - 25, py2 - 5);

        // Titik (-1.5,-2.5) - Biru
        double px3 = centerX + (-1.5) * scale;
        double py3 = centerY - (-2.5) * scale;
        gc.setFill(Color.BLUE);
        gc.fillOval(px3 - 3, py3 - 3, 6, 6);
        gc.strokeText("(-1.5,-2.5)", px3 - 30, py3 + 15);

        // Titik (0,0) - Ungu
        gc.setFill(Color.PURPLE);
        gc.fillOval(centerX - 3, centerY - 3, 6, 6);
        gc.strokeText("(0,0)", centerX + 5, centerY + 15);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("Cartesian Coordinate");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
