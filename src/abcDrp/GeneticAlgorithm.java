package abcDrp;

import java.util.*;

public class GeneticAlgorithm {

    private final double[][] distanceMatrix;
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final int maxGenerations;
    private List<Individual> population;
    private final Random rand = new Random();

    // Representasi satu solusi (rute)
    private static class Individual {

        List<Integer> route;
        double fitness;

        Individual(List<Integer> r) {
            this.route = new ArrayList<>(r);
        }
    }

    public GeneticAlgorithm(double[][] distanceMatrix,
            int populationSize,
            double crossoverRate,
            double mutationRate,
            int maxGenerations) {
        this.distanceMatrix = distanceMatrix;
        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.maxGenerations = maxGenerations;
        initPopulation();
    }

    private void initPopulation() {
        int n = distanceMatrix.length;
        population = new ArrayList<>(populationSize);
        List<Integer> base = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            base.add(i);
        }
        for (int i = 0; i < populationSize; i++) {
            Collections.shuffle(base, rand);
            List<Integer> route = new ArrayList<>();
            route.add(0);
            route.addAll(base);
            route.add(0);
            population.add(new Individual(route));
        }
        evaluatePopulation();
    }

    private void evaluatePopulation() {
        for (Individual ind : population) {
            ind.fitness = fitness(ind.route);
        }
    }

    private double fitness(List<Integer> route) {
        double total = 0;
        for (int i = 1; i < route.size(); i++) {
            total += distanceMatrix[route.get(i - 1)][route.get(i)];
        }
        return 1.0 / (1 + total);
    }

    // Tournament selection: pilih terbaik dari k acak
    private Individual tournamentSelect(int k) {
        Individual best = null;
        for (int i = 0; i < k; i++) {
            Individual cand = population.get(rand.nextInt(populationSize));
            if (best == null || cand.fitness > best.fitness) {
                best = cand;
            }
        }
        return new Individual(best.route);
    }

    // PMX crossover antara dua parent
    private List<Integer>[] pmxCrossover(List<Integer> p1, List<Integer> p2) {
        int n = p1.size(), cut1 = rand.nextInt(n - 2) + 1, cut2 = rand.nextInt(n - 2) + 1;
        if (cut2 < cut1) {
            int t = cut1;
            cut1 = cut2;
            cut2 = t;
        }

        List<Integer> c1 = new ArrayList<>(Collections.nCopies(n, -1));
        List<Integer> c2 = new ArrayList<>(Collections.nCopies(n, -1));
        // copy segmen
        for (int i = cut1; i <= cut2; i++) {
            c1.set(i, p2.get(i));
            c2.set(i, p1.get(i));
        }
        // fill sisa
        for (int i = 1; i < n - 1; i++) {
            if (i >= cut1 && i <= cut2) {
                continue;
            }
            // child1
            int gene = p1.get(i);
            while (c1.contains(gene)) {
                gene = p1.get(p2.indexOf(gene));
            }
            c1.set(i, gene);
            // child2
            gene = p2.get(i);
            while (c2.contains(gene)) {
                gene = p2.get(p1.indexOf(gene));
            }
            c2.set(i, gene);
        }
        // ujung 0 tetap
        c1.set(0, 0);
        c1.set(n - 1, 0);
        c2.set(0, 0);
        c2.set(n - 1, 0);
        return new List[]{c1, c2};
    }

    // Swap mutation
    private void mutate(List<Integer> route) {
        int i = rand.nextInt(route.size() - 2) + 1;
        int j = rand.nextInt(route.size() - 2) + 1;
        Collections.swap(route, i, j);
    }

    public List<Integer> run() {
        for (int gen = 0; gen < maxGenerations; gen++) {
            List<Individual> nextGen = new ArrayList<>();
            // Elitism: bawa solusi terbaik
            Individual best = Collections.max(population, Comparator.comparingDouble(ind -> ind.fitness));
            nextGen.add(new Individual(best.route));

            // buat offspring
            while (nextGen.size() < populationSize) {
                Individual parent1 = tournamentSelect(3);
                Individual parent2 = tournamentSelect(3);
                List<Integer> child1, child2;
                if (rand.nextDouble() < crossoverRate) {
                    List<Integer>[] off = pmxCrossover(parent1.route, parent2.route);
                    child1 = off[0];
                    child2 = off[1];
                } else {
                    child1 = new ArrayList<>(parent1.route);
                    child2 = new ArrayList<>(parent2.route);
                }
                if (rand.nextDouble() < mutationRate) {
                    mutate(child1);
                }
                if (rand.nextDouble() < mutationRate) {
                    mutate(child2);
                }

                nextGen.add(new Individual(child1));
                if (nextGen.size() < populationSize) {
                    nextGen.add(new Individual(child2));
                }
            }

            population = nextGen;
            evaluatePopulation();
        }
        // kembalikan best solution
        return Collections.max(population, Comparator.comparingDouble(ind -> ind.fitness)).route;
    }
}
