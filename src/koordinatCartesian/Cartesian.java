/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koordinatCartesian;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Cartesian extends Application {

    // Koordinat logika (Cartesian) untuk 10 titik
    private final double[][] logicalPoints = {
            {-4, 2}, {3, 5}, {1, 1}, {-2.5, -3.5}, {5, -2},
            {4, 6}, {-3, -4}, {6, 3}, {-5, 4}, {2, -5}
    };

    private final String[] vertexLabels = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private static final double SCALE = 50; // Skala untuk memperbesar tampilan
    private static final double OFFSET_X = 400; // Pusat diagram
    private static final double OFFSET_Y = 300;

    @Override
    public void start(Stage primaryStage) {

        double panjang = 800; // Ukuran window baru (panjang)
        double tinggi = 600; // Ukuran window baru (tinggi)

        Canvas kanvas = new Canvas(panjang, tinggi);
        GraphicsContext gc = kanvas.getGraphicsContext2D();

        // Gambar sumbu X dan Y
        drawAxes(gc, panjang, tinggi);

        // Gambar semua vertex dan label
        for(int i = 0; i < logicalPoints.length; i++) {
            drawVertex(gc, logicalPoints[i][0], logicalPoints[i][1], vertexLabels[i], Color.BLUE);
        }

        // Gambar semua edge dan bobot
        for(int i = 0; i < logicalPoints.length; i++) {
            for(int j = i + 1; j < logicalPoints.length; j++) {
                drawEdge(gc, logicalPoints[i], logicalPoints[j]);
            }
        }

        Pane root = new Pane(kanvas);
        primaryStage.setTitle("Graf Lengkap Berbobot");
        primaryStage.setScene(new Scene(root, panjang, tinggi));
        primaryStage.show();
    }

    // Method untuk menggambar sumbu
    private void drawAxes(GraphicsContext gc, double width, double height) {
        gc.setStroke(Color.GRAY);
        gc.strokeLine(OFFSET_X, 0, OFFSET_X, height); // Sumbu Y
        gc.strokeLine(0, OFFSET_Y, width, OFFSET_Y);  // Sumbu X
    }

    // Method untuk menggambar vertex
    private void drawVertex(GraphicsContext gc, double x, double y, String label, Color color) {
        double screenX = OFFSET_X + x * SCALE;
        double screenY = OFFSET_Y - y * SCALE;

        // Gambar titik
        gc.setFill(color);
        gc.fillOval(screenX - 5, screenY - 5, 10, 10);

        // Gambar label
        gc.setFill(Color.BLACK);
        gc.fillText(label, screenX + 8, screenY - 8);
    }

    // Method untuk menggambar edge dan bobot
    private void drawEdge(GraphicsContext gc, double[] p1, double[] p2) {
        double x1 = OFFSET_X + p1[0] * SCALE;
        double y1 = OFFSET_Y - p1[1] * SCALE;
        double x2 = OFFSET_X + p2[0] * SCALE;
        double y2 = OFFSET_Y - p2[1] * SCALE;

        // Gambar garis
        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeLine(x1, y1, x2, y2);

        // Hitung jarak
        double dx = p2[0] - p1[0];
        double dy = p2[1] - p1[1];
        double distance = Math.sqrt(dx*dx + dy*dy);

        // Hitung titik tengah
        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;

        // Gambar label jarak
        gc.setFill(Color.RED);
        gc.fillText(String.format("%.2f", distance), midX, midY);
    }

    public static void main(String[] args) {
        launch(args);
    }
}