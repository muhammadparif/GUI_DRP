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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ABCGraphApp3 extends Application {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Random random = new Random();

    private Canvas canvas;
    private GraphicsContext gc;

    private List<Integer> tspRoute;
    private List<List<Integer>> drpTrips;

    // Batas jarak maksimum per trip (battery constraint)
    private static final double BATTERY_LIMIT = 1000.0;

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

    // Buat semua edges lengkap
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

    // Jalankan ABC dan validasi DRP dengan battery limit
    private void solveRoutes() {
        if (vertices.size() < 2) {
            System.out.println("Minimal 2 titik untuk menjalankan algoritma.");
            return;
        }

        int n = vertices.size();
        // Buat matriks jarak (adjacency)
        double[][] adjacency = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adjacency[i][j] = distance(vertices.get(i), vertices.get(j));
            }
        }

        // Parameter ABC
        int popSize = 10;
        int maxIteration = 100;
        int limit = 20;

        // Inisialisasi penyimpanan terbaik
        int[] bestSolution = null;
        double bestFitness = 0;

        // Struktur populasi
        int[][] population = new int[popSize][];
        double[] distanceArr = new double[popSize];
        double[] fitnessArr = new double[popSize];
        int[] trial = new int[popSize];

        // === Inisialisasi populasi awal + elitism ===
        for (int i = 0; i < popSize; i++) {
            int[] sol = randomSolution(adjacency);
            population[i] = sol;

            // Hitung total jarak rute
            double d = calculateDistanceValid(toList(sol), adjacency);
            distanceArr[i] = d;
            fitnessArr[i] = 1.0 / (1.0 + d);

            if (fitnessArr[i] > bestFitness) {
                bestFitness = fitnessArr[i];
                bestSolution = sol.clone();
            }
        }

        // === Siklus ABC ===
        for (int iter = 1; iter <= maxIteration; iter++) {
            // --- Employed Bees ---
            for (int i = 0; i < popSize; i++) {
                int[] cand = population[i].clone();
                int idx1 = randomBetween(1, cand.length - 2);
                int idx2 = randomBetween(1, cand.length - 2);
                while (idx2 == idx1) {
                    idx2 = randomBetween(1, cand.length - 2);
                }
                // Swap manual
                int tmp = cand[idx1];
                cand[idx1] = cand[idx2];
                cand[idx2] = tmp;

                double d = calculateDistanceValid(toList(cand), adjacency);
                double f = 1.0 / (1.0 + d);
                if (f > fitnessArr[i]) {
                    population[i] = cand;
                    distanceArr[i] = d;
                    fitnessArr[i] = f;
                    trial[i] = 0;
                } else {
                    trial[i]++;
                }
            }

            // --- Onlooker Bees ---
            double sumFit = 0;
            for (double f : fitnessArr) sumFit += f;
            int k = 0;
            while (k < popSize) {
                for (int i = 0; i < popSize && k < popSize; i++) {
                    double prob = fitnessArr[i] / sumFit;
                    if (random.nextDouble() < prob) {
                        k++;
                        int[] cand = population[i].clone();
                        int idx1 = randomBetween(1, cand.length - 2);
                        int idx2 = randomBetween(1, cand.length - 2);
                        while (idx2 == idx1) {
                            idx2 = randomBetween(1, cand.length - 2);
                        }
                        // Insert operation
                        int temp = cand[idx1];
                        if (idx1 < idx2) {
                            for (int j = idx1; j < idx2; j++) {
                                cand[j] = cand[j + 1];
                            }
                            cand[idx2] = temp;
                        } else {
                            for (int j = idx1; j > idx2; j--) {
                                cand[j] = cand[j - 1];
                            }
                            cand[idx2] = temp;
                        }
                        double d = calculateDistanceValid(toList(cand), adjacency);
                        double f = 1.0 / (1.0 + d);
                        if (f > fitnessArr[i]) {
                            population[i] = cand;
                            distanceArr[i] = d;
                            fitnessArr[i] = f;
                            trial[i] = 0;
                        } else {
                            trial[i]++;
                        }
                    }
                }
            }

            // --- Scout Bees ---
            for (int i = 0; i < popSize; i++) {
                if (trial[i] > limit) {
                    int[] sol = randomSolution(adjacency);
                    population[i] = sol;
                    double d = calculateDistanceValid(toList(sol), adjacency);
                    distanceArr[i] = d;
                    fitnessArr[i] = 1.0 / (1.0 + d);
                    trial[i] = 0;
                }
            }

            // --- Update Elitism ---
            for (int i = 0; i < popSize; i++) {
                if (fitnessArr[i] > bestFitness) {
                    bestFitness = fitnessArr[i];
                    bestSolution = population[i].clone();
                }
            }
        }

        // Konversi bestSolution ke List<Integer> untuk menggambar TSP
        tspRoute = toList(bestSolution);

        // Validasi rute berdasarkan battery limit
        List<Integer> validRoute = validasiSolusi(bestSolution, adjacency, BATTERY_LIMIT);
        drpTrips = new ArrayList<>();
        drpTrips.add(validRoute);

        System.out.println("TSP (raw): " + tspRoute);
        System.out.println("DRP (valid)Idx: " + validRoute);

        redrawAll();
    }

    // Helper: ubah array ke List<Integer>
    private static List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int v : arr) list.add(v);
        return list;
    }

    // Generate solusi acak (mulai/akhir di depot = indeks 3)
    public static int[] randomSolution(double[][] adjacency) {
        int n = adjacency.length;
        int[] sol = new int[n + 1];
        int depot = 3;
        sol[0] = depot;
        sol[n] = depot;
        for (int i = 1; i < n; i++) {
            int candidate;
            boolean unique;
            do {
                candidate = randomBetween(0, n - 1);
                unique = true;
                for (int j = 0; j < i; j++) {
                    if (sol[j] == candidate) {
                        unique = false;
                        break;
                    }
                }
            } while (!unique);
            sol[i] = candidate;
        }
        return sol;
    }

    // Generate integer di [min..max]
    public static int randomBetween(int min, int max) {
        if (min > max) {
            int t = min; min = max; max = t;
        }
        return min + new Random().nextInt(max - min + 1);
    }

    // Validasi rute: hanya ambil segmen yang masih memungkinkan kembali ke depot
    public static List<Integer> validasiSolusi(int[] sol, double[][] adj, double jarMaxBat) {
        List<Integer> valid = new ArrayList<>();
        int depot = sol[0];
        valid.add(depot);
        double used = 0;
        for (int k = 1; k < sol.length; k++) {
            int v = sol[k];
            double seg = adj[ valid.get(valid.size()-1) ][ v ];
            double back = adj[v][depot];
            if (used + seg <= jarMaxBat && back <= (jarMaxBat - (used + seg))) {
                used += seg;
                valid.add(v);
            } else {
                valid.add(depot);
                break;
            }
        }
        return valid;
    }

    // Hitung total jarak rute valid
    public static double calculateDistanceValid(List<Integer> solValid, double[][] adj) {
        double total = 0;
        for (int i = 1; i < solValid.size(); i++) {
            int a = solValid.get(i-1), b = solValid.get(i);
            total += adj[a][b];
        }
        return total;
    }

    // Gambar ulang semua elemen di canvas
    private void redrawAll() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // edges (Light Gray)
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (Edge e : edges) {
            Vertex a = vertices.get(e.v1);
            Vertex b = vertices.get(e.v2);
            gc.strokeLine(a.x, a.y, b.x, b.y);
        }

        // TSP route (Blue)
        if (tspRoute != null) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            gc.setLineDashes(0);
            for (int i = 1; i < tspRoute.size(); i++) {
                Vertex a = vertices.get(tspRoute.get(i-1));
                Vertex b = vertices.get(tspRoute.get(i));
                gc.strokeLine(a.x, a.y, b.x, b.y);
            }
        }

        // DRP trips (Red dashed)
        if (drpTrips != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.setLineDashes(10);
            for (List<Integer> trip : drpTrips) {
                for (int i = 1; i < trip.size(); i++) {
                    Vertex a = vertices.get(trip.get(i-1));
                    Vertex b = vertices.get(trip.get(i));
                    gc.strokeLine(a.x, a.y, b.x, b.y);
                }
            }
            gc.setLineDashes(0);
        }

        // Nodes
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            gc.setFill(i == 0 ? Color.GOLD : Color.DARKORANGE);
            gc.fillOval(v.x - 8, v.y - 8, 16, 16);
            gc.setFill(Color.BLACK);
            gc.fillText("V" + i, v.x - 10, v.y - 10);
        }
    }

    // Reset semua data
    private void clearAll() {
        vertices.clear();
        edges.clear();
        tspRoute = null;
        drpTrips = null;
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        System.out.println("Semua data telah direset.");
    }

    // Hitung jarak Euclidean dua vertex
    private double distance(Vertex a, Vertex b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    // Nested kelas untuk Vertex
    private static class Vertex {
        double x, y;
        Vertex(double x, double y) { this.x = x; this.y = y; }
    }

    // Nested kelas untuk Edge
    private static class Edge {
        int v1, v2;
        Edge(int a, int b) { v1 = a; v2 = b; }
    }
}
