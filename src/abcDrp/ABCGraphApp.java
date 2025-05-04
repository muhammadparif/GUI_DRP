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

public class ABCGraphApp extends Application {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Random random = new Random();

    private Canvas canvas;
    private GraphicsContext gc;

    private List<Integer> tspRoute;
    private List<Integer> drpRoute;
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
        System.out.println("\n=== SOLVING ROUTES ===");
        double[][] distanceMatrix = createDistanceMatrix();

        ABCAlgorithm abc = new ABCAlgorithm(distanceMatrix, 20, 10, 100);
        tspRoute = abc.optimize();
        System.out.println("Best Route (ABC): " + tspRoute);
        System.out.println("Distance: " + String.format("%.2f", calculateRouteDistance(tspRoute)) + "m");

        validateDRPRoute();
        draw();
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

    private void validateDRPRoute() {
        drpRoute = new ArrayList<>();
        if (tspRoute == null || tspRoute.size() < 3) return;

        double usedBattery = 0;
        int depot = 0;
        drpRoute.add(depot);

        for (int i = 1; i < tspRoute.size(); i++) {
            int current = tspRoute.get(i - 1);
            int next = tspRoute.get(i);

            double segmentDist = distance(vertices.get(current), vertices.get(next));
            double returnDist = distance(vertices.get(next), vertices.get(depot));

            if (usedBattery + segmentDist + returnDist <= BATTERY_CAPACITY) {
                usedBattery += segmentDist;
                drpRoute.add(next);
            } else {
                drpRoute.add(depot);
                break;
            }
        }
        System.out.println("DRP Valid Route: " + drpRoute);
        System.out.println("DRP Distance: " + String.format("%.2f", calculateRouteDistance(drpRoute)) + "m");
        System.out.println("Battery used: " + String.format("%.2f", usedBattery) + "m");
        System.out.println("Battery remaining: " + String.format("%.2f", (BATTERY_CAPACITY - usedBattery)) + "m");
    }

    private double distance(Vertex a, Vertex b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private void draw() {
        clearCanvas();
        drawEdges();
        drawRoutes();
        drawNodes();
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

    private void drawRoutes() {
        if (tspRoute != null) drawRoute(tspRoute, Color.BLUE, 3);
        if (drpRoute != null) drawRoute(drpRoute, Color.RED, 5);
    }

    private void drawRoute(List<Integer> route, Color color, double width) {
        gc.setStroke(color);
        gc.setLineWidth(width);
        for (int i = 1; i < route.size(); i++) {
            Vertex start = vertices.get(route.get(i - 1));
            Vertex end = vertices.get(route.get(i));
            gc.strokeLine(start.x, start.y, end.x, end.y);
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
