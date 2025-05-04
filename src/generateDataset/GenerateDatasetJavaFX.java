package generateDataset;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.*;

public class GenerateDatasetJavaFX extends Application {
    
    // Method untuk menghasilkan angka acak antara min dan max (bilangan desimal)
    public static double randomTitik(double min, double max) {
        Random r = new Random();
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return min + (max - min) * r.nextDouble();
    }

    @Override
    public void start(Stage primaryStage) {
        Random random = new Random();
        List<String> dataset = new ArrayList<>(); // Menggunakan ArrayList untuk menyimpan titik

        // Nilai yang sudah ditentukan
        int numVertices = 10; // Misalnya 10 titik
        double width = 400.0; // Lebar area canvas
        double height = 400.0; // Tinggi area canvas

        while (dataset.size() < numVertices) {
            double x = randomTitik(0, width);
            double y = randomTitik(0, height);

            // Menggabungkan x dan y menjadi satu string
            String point = String.format("%.2f,%.2f", x, y);

            // Memastikan titik tidak ada duplikat
            if (!dataset.contains(point)) {
                dataset.add(point);
            }
        }

        // Membuat Canvas untuk menggambar titik-titik
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Menggambar titik-titik pada canvas
        for (int i = 0; i < dataset.size(); i++) {
            String point = dataset.get(i);
            String[] coords = point.split(",");
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);

            // Menggambar titik pada posisi (x, y)
            gc.fillOval(x, y, 5, 5); // Titik digambar sebagai oval kecil
        }

        // Menyiapkan layout dan scene
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 500, 500);

        // Menyiapkan stage (jendela aplikasi)
        primaryStage.setTitle("Generate Dataset with JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
