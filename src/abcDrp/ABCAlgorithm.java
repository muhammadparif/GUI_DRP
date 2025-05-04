package abcDrp;

import java.util.*;

public class ABCAlgorithm {
    private final int foodNumber; // jumlah solusi
    private final int limit;
    private final int maxCycle;
    private final double[][] distanceMatrix;
    private final Random rand = new Random();

    private List<Integer>[] foodSources;
    private double[] fitness;
    private int[] trial;
    private List<Integer> bestSolution;
    private double bestFitness = Double.NEGATIVE_INFINITY;

    public ABCAlgorithm(double[][] distanceMatrix, int foodNumber, int limit, int maxCycle) {
        this.distanceMatrix = distanceMatrix;
        this.foodNumber = foodNumber;
        this.limit = limit;
        this.maxCycle = maxCycle;
        init();
    }

    private void init() {
        int n = distanceMatrix.length;
        foodSources = new ArrayList[foodNumber];
        fitness = new double[foodNumber];
        trial = new int[foodNumber];

        for (int i = 0; i < foodNumber; i++) {
            foodSources[i] = generateRandomSolution(n);
            fitness[i] = calculateFitness(foodSources[i]);
            trial[i] = 0;
            updateBest(i);
        }
    }

    public List<Integer> optimize() {
        for (int cycle = 0; cycle < maxCycle; cycle++) {
            employedBeesPhase();
            onlookerBeesPhase();
            scoutBeesPhase();
        }
        return bestSolution;
    }

    private void employedBeesPhase() {
        for (int i = 0; i < foodNumber; i++) {
            exploreNeighborhood(i);
        }
    }

    private void onlookerBeesPhase() {
        double sumFitness = Arrays.stream(fitness).sum();
        for (int i = 0; i < foodNumber; i++) {
            double r = rand.nextDouble();
            int index = selectFoodSource(sumFitness, r);
            exploreNeighborhood(index);
        }
    }

    private void scoutBeesPhase() {
        for (int i = 0; i < foodNumber; i++) {
            if (trial[i] > limit) {
                foodSources[i] = generateRandomSolution(distanceMatrix.length);
                fitness[i] = calculateFitness(foodSources[i]);
                trial[i] = 0;
                updateBest(i);
            }
        }
    }

    private void exploreNeighborhood(int i) {
        List<Integer> newSol = new ArrayList<>(foodSources[i]);
        int a = rand.nextInt(newSol.size());
        int b = rand.nextInt(newSol.size());
        Collections.swap(newSol, a, b);

        double newFit = calculateFitness(newSol);
        if (newFit > fitness[i]) {
            foodSources[i] = newSol;
            fitness[i] = newFit;
            trial[i] = 0;
            updateBest(i);
        } else {
            trial[i]++;
        }
    }

    private List<Integer> generateRandomSolution(int n) {
        List<Integer> route = new ArrayList<>();
        for (int i = 1; i < n; i++) route.add(i);
        Collections.shuffle(route);
        route.add(0, 0);
        route.add(0);
        return route;
    }

    private double calculateFitness(List<Integer> route) {
        double totalDistance = 0;
        for (int i = 1; i < route.size(); i++) {
            int from = route.get(i - 1);
            int to = route.get(i);
            totalDistance += distanceMatrix[from][to];
        }
        return 1.0 / (1 + totalDistance);
    }

    private int selectFoodSource(double sumFitness, double r) {
        double cumulative = 0;
        for (int i = 0; i < foodNumber; i++) {
            cumulative += fitness[i] / sumFitness;
            if (r <= cumulative) return i;
        }
        return rand.nextInt(foodNumber);
    }

    private void updateBest(int i) {
        if (fitness[i] > bestFitness) {
            bestFitness = fitness[i];
            bestSolution = new ArrayList<>(foodSources[i]);
        }
    }
} 
