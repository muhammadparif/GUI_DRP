package buatpoints;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LargeGraphVisualization extends Application {

    private static final int NODE_COUNT = 100; // Jumlah node
    private static final int RADIUS = 5; // Radius node (diperkecil untuk 100 node)
    private static final int WIDTH = 900; // Lebar window (diperbesar untuk 100 node)
    private static final int HEIGHT = 800; // Tinggi window (diperbesar untuk 100 node)

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Generate matriks adjacency acak
        double[][] adjacencyMatrix = generateRandomAdjacencyMatrix(NODE_COUNT);

        // Generate posisi node acak
        double[][] positions = generateRandomPositions(NODE_COUNT, WIDTH, HEIGHT);

        // Buat graf
        Graph graph = new Graph(adjacencyMatrix, positions);

        // Tambahkan node dan edge ke pane
        for (Node node : graph.getNodes()) {
            root.getChildren().add(node.getCircle());
            root.getChildren().add(node.getLabel());
        }
        for (Edge edge : graph.getEdges()) {
            root.getChildren().add(edge.getLine());
            // Opsional: Tampilkan berat edge jika diperlukan
            // root.getChildren().add(edge.getWeightLabel());
        }

        // Set judul dan tampilkan stage
        primaryStage.setTitle("Large Graph Visualization with 100 Nodes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Generate matriks adjacency acak
    private double[][] generateRandomAdjacencyMatrix(int size) {
        double[][] matrix = new double[size][size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (random.nextDouble() < 0.1) { // Probabilitas edge = 10%
                    double weight = 1 + random.nextDouble() * 99; // Berat edge antara 1 dan 100
                    matrix[i][j] = weight;
                    matrix[j][i] = weight; // Graf tak berarah
                }
            }
        }
        return matrix;
    }

    // Generate posisi node acak
    private double[][] generateRandomPositions(int size, int width, int height) {
        double[][] positions = new double[size][2];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            positions[i][0] = random.nextDouble() * width; // Posisi x acak
            positions[i][1] = random.nextDouble() * height; // Posisi y acak
        }
        return positions;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Kelas Node
class Node {
    private final int id;
    private final double x;
    private final double y;
    private final Circle circle;
    private final Text label;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;

        // Buat lingkaran untuk node
        this.circle = new Circle(x, y, 5, Color.LIGHTGRAY); // Radius diperkecil

        // Buat label untuk node
        this.label = new Text(x - 5, y + 5, "V" + id);
    }

    public Circle getCircle() {
        return circle;
    }

    public Text getLabel() {
        return label;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}

// Kelas Edge
class Edge {
    private final Line line;
    private final Text weightLabel;

    public Edge(Node start, Node end, double weight) {
        // Buat garis untuk edge
        this.line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        this.line.setStroke(Color.BLUE);

        // Buat label untuk berat edge
        double midX = (start.getX() + end.getX()) / 2;
        double midY = (start.getY() + end.getY()) / 2;
        this.weightLabel = new Text(midX, midY, String.format("%.2f", weight));
    }

    public Line getLine() {
        return line;
    }

    public Text getWeightLabel() {
        return weightLabel;
    }
}

// Kelas Graph
class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;

    public Graph(double[][] adjacencyMatrix, double[][] positions) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();

        // Buat node
        for (int i = 0; i < positions.length; i++) {
            nodes.add(new Node(i, positions[i][0], positions[i][1]));
        }

        // Buat edge berdasarkan matriks adjacency
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = i + 1; j < adjacencyMatrix[i].length; j++) {
                if (adjacencyMatrix[i][j] != 0) {
                    edges.add(new Edge(nodes.get(i), nodes.get(j), adjacencyMatrix[i][j]));
                }
            }
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}