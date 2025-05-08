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

public class ABCGraphApp2 extends Application {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Random random = new Random();

    private Canvas canvas;
    private GraphicsContext gc;

    private List<Integer> tspRoute;
    private List<List<Integer>> drpTrips;
    private static final double BATTERY_CAPACITY = 1000.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Root layout
        BorderPane root = new BorderPane();

        // Canvas setup
        canvas = new Canvas(1024, 768);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        root.setCenter(canvas);

        // Buttons
        Button addBtn = new Button("Add Node");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> addVertex());

        Button edgeBtn = new Button("Create Edges");
        edgeBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        edgeBtn.setOnAction(e -> createEdges());

        Button solveBtn = new Button("Solve Routes");
        solveBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold;");
        solveBtn.setOnAction(e -> solveRoutes());

        Button clearBtn = new Button("Clear All");
        clearBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setOnAction(e -> clearAll());

        // Toolbar layout
        HBox toolbar = new HBox(10, addBtn, edgeBtn, solveBtn, clearBtn);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: #333;");
        root.setTop(toolbar);

        // Show stage
        Scene scene = new Scene(root, 1024, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Drone Routing Visualizer");
        primaryStage.show();
    }

    // Tambah simpul acak
    private void addVertex() {
        double x = 100 + random.nextDouble() * 700;
        double y = 100 + random.nextDouble() * 500;
        vertices.add(new Vertex(x, y));
        System.out.println(String.format("Tambah titik: (%.1f, %.1f)", x, y));
        redrawAll();
    }

    // Buat semua edges
    private void createEdges() {
        edges.clear();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                edges.add(new Edge(i, j));
            }
        }
        System.out.println("Edges dibuat: " + edges.size());
        redrawAll();
    }

    // Jalankan algoritma ABC dan DRP
    private void solveRoutes() {
        if (vertices.size() < 2) {
            System.out.println("Minimal 2 titik untuk menjalankan algoritma.");
            return;
        }
        // Matriks jarak
        int n = vertices.size();
        double[][] distMat = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distMat[i][j] = distance(vertices.get(i), vertices.get(j));
            }
        }
        // Algoritma ABC
        System.out.println("=== Memulai Optimasi ABC ===");
        ABCAlgorithm abc = new ABCAlgorithm(distMat, 20, 10, 50);
        tspRoute = abc.optimize();
        System.out.println("Rute akhir TSP (ABC): " + tspRoute);

        // Validasi DRP
        System.out.println("=== Memulai Validasi DRP ===");
        drpTrips = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        double batteryUsed = 0;  // baterai terpakai
        int depot = 0;
        int current = depot;
        List<Integer> trip = new ArrayList<>();
        trip.add(depot);
        for (int k = 1; k < tspRoute.size() - 1; k++) {
            int nextVertex = tspRoute.get(k);
            if (visited.contains(nextVertex)) {
                continue;
            }
            double segmentDistance = distance(vertices.get(current), vertices.get(nextVertex));
            double returnDistance = distance(vertices.get(nextVertex), vertices.get(depot));
            System.out.printf("Cek V%d: jarakSegmen=%.2f, jarakPulang=%.2f, bateraiTerpakai=%.2f%n",
                    nextVertex, segmentDistance, returnDistance, batteryUsed);
            if (batteryUsed + segmentDistance + returnDistance <= BATTERY_CAPACITY) {
                batteryUsed += segmentDistance;
                trip.add(nextVertex);
                visited.add(nextVertex);
                current = nextVertex;
                System.out.printf("  Terima V%d (sisaBaterai=%.2f)%n", nextVertex, BATTERY_CAPACITY - batteryUsed);
            } else {
                System.out.printf("  Tolak V%d (akan melebihi kapasitas baterai)%n", nextVertex);
                break;
            }
        }
        if (trip.size() > 1) {
            trip.add(depot);
            drpTrips.add(trip);
            System.out.println("Kembali ke depot: " + trip);
        }
        redrawAll();
        System.out.println("=== Selesai Validasi DRP ===");
    }

    // Gambar ulang semua elemen
    private void redrawAll() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // edges
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (Edge e : edges) {
            Vertex a = vertices.get(e.v1);
            Vertex b = vertices.get(e.v2);
            gc.strokeLine(a.x, a.y, b.x, b.y);
        }
        
        // rute TSP
        if (tspRoute != null) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            gc.setLineDashes(0);
            for (int i = 1; i < tspRoute.size(); i++) {
                Vertex a = vertices.get(tspRoute.get(i - 1));
                Vertex b = vertices.get(tspRoute.get(i));
                gc.strokeLine(a.x, a.y, b.x, b.y);
            }
        }
        // rute DRP
        if (drpTrips != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.setLineDashes(10);
            for (List<Integer> t : drpTrips) {
                for (int i = 1; i < t.size(); i++) {
                    Vertex a = vertices.get(t.get(i - 1));
                    Vertex b = vertices.get(t.get(i));
                    gc.strokeLine(a.x, a.y, b.x, b.y);
                }
            }
            gc.setLineDashes(0);
        }
        // nodes
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            gc.setFill(i == 0 ? Color.GOLD : Color.DARKORANGE);
            gc.fillOval(v.x - 8, v.y - 8, 16, 16);
            gc.setFill(Color.BLACK);
            gc.fillText("V" + i, v.x - 10, v.y - 10);
        }
    }

    // Bersihkan semua data
    private void clearAll() {
        vertices.clear();
        edges.clear();
        tspRoute = null;
        drpTrips = null;
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        System.out.println("Semua data telah direset.");
    }

    // Hitung jarak Euclidean
    private double distance(Vertex a, Vertex b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    // Nested class ABCAlgorithm
    public static class ABCAlgorithm {

        private final int foodNumber, limit, maxCycle;
        private final double[][] distMat;
        private final Random rand = new Random();
        private List<List<Integer>> foodSources;
        private double[] fitness;
        private int[] trial;
        private List<Integer> bestSol;
        private double bestFit = Double.NEGATIVE_INFINITY;

        public ABCAlgorithm(double[][] distMat, int foodNumber, int limit, int maxCycle) {
            this.distMat = distMat;
            this.foodNumber = foodNumber;
            this.limit = limit;
            this.maxCycle = maxCycle;
            init();
        }

        private void init() {
            int n = distMat.length;
            foodSources = new ArrayList<>();
            fitness = new double[foodNumber];
            trial = new int[foodNumber];
            for (int i = 0; i < foodNumber; i++) {
                List<Integer> sol = randomSolution(n);
                foodSources.add(sol);
                fitness[i] = calcFit(sol);
                trial[i] = 0;
                if (fitness[i] > bestFit) {
                    bestFit = fitness[i];
                    bestSol = new ArrayList<>(sol);
                }
            }
        }

        public List<Integer> optimize() {
            for (int cycle = 1; cycle <= maxCycle; cycle++) {
                System.out.println("--- Siklus " + cycle + " ---");
                // Employed Bees
                System.out.println("Fase Employed Bees");
                for (int i = 0; i < foodNumber; i++) {
                    neighborSearch(i);
                }
                // Cetak solusi setelah Employed
                System.out.println("Solusi setelah Employed: " + foodSources);

                // Onlooker Bees
                System.out.println("Fase Onlooker Bees");
                double sumFitness = Arrays.stream(fitness).sum();
                for (int i = 0; i < foodNumber; i++) {
                    double r = rand.nextDouble(), threshold = 0;
                    for (int j = 0; j < foodNumber; j++) {
                        threshold += fitness[j] / sumFitness;
                        if (r <= threshold) {
                            neighborSearch(j);
                            break;
                        }
                    }
                }
                // Cetak solusi setelah Onlooker
                System.out.println("Solusi setelah Onlooker: " + foodSources);

                // Scout Bees
                System.out.println("Fase Scout Bees");
                for (int i = 0; i < foodNumber; i++) {
                    if (trial[i] > limit) {
                        System.out.println("Scout mengganti solusi ke-" + i);
                        List<Integer> newSol = randomSolution(distMat.length);
                        foodSources.set(i, newSol);
                        fitness[i] = calcFit(newSol);
                        trial[i] = 0;
                        if (fitness[i] > bestFit) {
                            bestFit = fitness[i];
                            bestSol = new ArrayList<>(newSol);
                        }
                    }
                }
                // Cetak solusi setelah Scout
                System.out.println("Solusi setelah Scout: " + foodSources);

                List<Integer> disp = ensureDepot(bestSol);
                System.out.println(String.format(
                        "Siklus %d selesai. Best fitness=%.6f, Best rute=%s",
                        cycle, bestFit, disp
                ));
            }
            System.out.println("=== Optimasi ABC Selesai ===");
            return ensureDepot(bestSol);
        }

        private void neighborSearch(int i) {
            List<Integer> sol = new ArrayList<>(foodSources.get(i));
            int len = sol.size();
            if (len > 2) {
                int a = 1 + rand.nextInt(len - 2);
                int b = 1 + rand.nextInt(len - 2);
                Collections.swap(sol, a, b);
                double f = calcFit(sol);
                if (f > fitness[i]) {
                    foodSources.set(i, sol);
                    fitness[i] = f;
                    trial[i] = 0;
                    if (f > bestFit) {
                        bestFit = f;
                        bestSol = new ArrayList<>(sol);
                    }
                } else {
                    trial[i]++;
                }
            }
        }

        private List<Integer> randomSolution(int n) {
            List<Integer> r = new ArrayList<>();
            for (int i = 1; i < n; i++) {
                r.add(i);
            }
            Collections.shuffle(r);
            r.add(0, 0);
            r.add(0);
            return r;
        }

        private double calcFit(List<Integer> route) {
            double d = 0;
            for (int i = 1; i < route.size(); i++) {
                d += distMat[route.get(i - 1)][route.get(i)];
            }
            return 1.0 / (1 + d);
        }

        private List<Integer> ensureDepot(List<Integer> route) {
            List<Integer> r = new ArrayList<>(route);
            if (r.get(0) != 0) {
                r.add(0, 0);
            }
            if (r.get(r.size() - 1) != 0) {
                r.add(0);
            }
            return r;
        }
    }

    private static class Vertex {

        double x, y;

        Vertex(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Edge {

        int v1, v2;

        Edge(int a, int b) {
            v1 = a;
            v2 = b;
        }
    }
}
