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
public class Latihan001 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        int lebar = 400;
        int tinggi = 400;
        
        Canvas canvas = new Canvas(lebar, tinggi);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        int centerX = lebar / 2;
        int centerY = tinggi / 2;
        
        // gambar sumbu x dan Y
        gc.strokeLine(0, centerY, lebar, centerY);
        gc.strokeLine(centerX, 0, centerX, tinggi);
        
        int scale = 30;
        gc.setLineWidth(1);
        gc.setStroke(Color.GREY);
        
        //gambar titik (2,3)
        double px1 = centerX + 2 * scale;
        double py1 = centerY - 3 * scale;
        gc.setFill(Color.ORANGE);
        gc.fillOval(px1 - 3, py1 - 3, 6, 6);
        gc.strokeText("(2,3)", px1 + 5, py1 -5);
        
        //gambar (-3, 1)
        double px2 = centerX + (-3) * scale;
        double py2 = centerY - 1 * scale;
        gc.setFill(Color.BLACK);
        gc.fillOval(px2 - 3, py2 - 3, 6, 6);
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, lebar, tinggi);
        primaryStage.setTitle("Latihan 001");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
