package abcDrp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class GAGraphApp extends Application {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Random random = new Random();

    private Canvas canvas;
    private GraphicsContext gc;

    private List<Integer> tspRoute;
    private List<List<Integer>> drpTrips;
    private final double BATTERY_CAPACITY = 1000.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        setupCanvas(root);
        setupToolbar(root);
        setupStage(primaryStage, root);
    }

    private void setupCanvas(BorderPane root) {
        canvas = new Canvas(1024, 768);
        gc = canvas.getGraphicsContext2D();
        clearCanvas();
        root.setCenter(canvas);
    }

    private Button createButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void setupToolbar(BorderPane root) {
        Button addBtn = createButton("Add Node", "#4CAF50", this::addVertex);
        Button edgeBtn = createButton("Create Edges", "#2196F3", this::createEdges);
        Button solveBtn = createButton("Solve Routes", "#9C27B0", this::solveRoutes);
        Button clearBtn = createButton("Clear All", "#F44336", this::clearCanvas);

        HBox toolbar = new HBox(10, addBtn, edgeBtn, solveBtn, clearBtn);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: #333;");
        root.setTop(toolbar);
    }

    private void setupStage(Stage stage, BorderPane root) {
        Scene scene = new Scene(root, 1024, 800);
        stage.setScene(scene);
        stage.setTitle("Drone Routing Visualizer");
        stage.show();
    }

    private void addVertex() {
        double x = 100 + random.nextDouble() * 700;
        double y = 100 + random.nextDouble() * 500;
        vertices.add(new Vertex(x, y));
        System.out.println("Added new vertex at (" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")");
        System.out.println("Total nodes: " + vertices.size());
        draw();
    }

    private void createEdges() {
        edges.clear();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                edges.add(new Edge(i, j));
            }
        }
        System.out.println("\n=== EDGES CREATED ===");
        System.out.println("Total edges: " + edges.size());
        draw();
    }

    private void solveRoutes() {
        if (vertices.size() < 2) {
            System.out.println("Add at least 2 nodes to solve routes.");
            return;
        }
        double[][] distanceMatrix = createDistanceMatrix();
        GeneticAlgorithm ga = new GeneticAlgorithm(distanceMatrix,
                50, // population size
                0.8, // crossover rate
                0.1, // mutation rate
                500); // max generations
        tspRoute = sanitizeTSPRoute(ga.run());
        System.out.println("Route (ABC): " + tspRoute);
        planTrips();
        draw();
    }

    private List<Integer> sanitizeTSPRoute(List<Integer> original) {
        Set<Integer> visited = new HashSet<>();
        List<Integer> result = new ArrayList<>();
        result.add(0);
        for (Integer node : original) {
            if (node != 0 && visited.add(node)) {
                result.add(node);
            }
        }
        if (result.get(result.size() - 1) != 0) {
            result.add(0);
        }
        return result;
    }

    private double[][] createDistanceMatrix() {
        int n = vertices.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = distance(vertices.get(i), vertices.get(j));
            }
        }
        return matrix;
    }

    private void planTrips() {
        drpTrips = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        double usedBattery = 0;
        int depot = 0;
        int current = depot;
        List<Integer> trip = new ArrayList<>();
        trip.add(depot);

        System.out.println("\nSegment Log:");

        for (int i = 1; i < tspRoute.size() - 1; i++) {
            int next = tspRoute.get(i);
            if (visited.contains(next)) {
                continue;
            }

            double segmentDist = distance(vertices.get(current), vertices.get(next));
            double returnDist = distance(vertices.get(next), vertices.get(depot));

            if (usedBattery + segmentDist + returnDist <= BATTERY_CAPACITY) {
                usedBattery += segmentDist;
                trip.add(next);
                visited.add(next);
                System.out.printf("V%d -> V%d: %.2f m | Sisa Baterai: %.2f m\n", current, next, segmentDist, BATTERY_CAPACITY - usedBattery);
                current = next;
            } else {
                break;
            }
        }

        if (trip.size() > 1 && trip.get(trip.size() - 1) != depot) {
            trip.add(depot);
            drpTrips.add(trip);
            System.out.printf("Kembali ke V%d (akhir)\n", depot);
        }

        System.out.println("Valid DRP Trip: " + drpTrips);
        double total = 0;
        for (List<Integer> t : drpTrips) {
            total += calculateRouteDistance(t);
        }
        System.out.println("Total DRP Distance: " + String.format("%.2f", total) + "m");
    }

    private void draw() {
        clearCanvas();
        drawEdges();
        drawRoute(tspRoute, Color.BLUE, 2, false);
        if (drpTrips != null) {
            for (List<Integer> trip : drpTrips) {
                drawRoute(trip, Color.RED, 2, true);
            }
        }
        drawNodes();
    }

    private void drawRoute(List<Integer> route, Color color, double width, boolean dashed) {
        if (route == null || route.size() < 2) {
            return;
        }
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.setLineDashes(dashed ? 10 : 0);
        for (int i = 1; i < route.size(); i++) {
            Vertex start = vertices.get(route.get(i - 1));
            Vertex end = vertices.get(route.get(i));
            gc.strokeLine(start.x, start.y, end.x, end.y);
        }
        gc.setLineDashes(0);
    }

    private void drawEdges() {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (Edge edge : edges) {
            Vertex v1 = vertices.get(edge.v1);
            Vertex v2 = vertices.get(edge.v2);
            gc.strokeLine(v1.x, v1.y, v2.x, v2.y);
        }
    }

    private void drawNodes() {
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            gc.setFill(i == 0 ? Color.GOLD : Color.DARKORANGE);
            gc.fillOval(v.x - 8, v.y - 8, 16, 16);
            gc.setFill(Color.BLACK);
            gc.fillText("V" + i, v.x - 10, v.y - 10);
        }
    }

    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private double calculateRouteDistance(List<Integer> route) {
        double total = 0;
        for (int i = 1; i < route.size(); i++) {
            int from = route.get(i - 1);
            int to = route.get(i);
            total += distance(vertices.get(from), vertices.get(to));
        }
        return total;
    }

    private double distance(Vertex a, Vertex b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private static class Vertex {

        public double x, y;

        Vertex(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Edge {

        public int v1, v2;

        Edge(int v1, int v2) {
            this.v1 = v1;
            this.v2 = v2;
        }
    }
}
