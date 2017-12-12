/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp;

import edu.gsgp.data.ExperimentalData;
import edu.gsgp.nodes.Node;
import edu.gsgp.nodes.functions.*;
import edu.gsgp.nodes.terminals.ERC;
import edu.gsgp.population.GSGPIndividual;
import edu.gsgp.population.Population;
import edu.gsgp.population.Individual;
import edu.gsgp.data.PropertiesManager;
import edu.gsgp.population.fitness.Fitness;
import edu.gsgp.population.populator.Populator;
import edu.gsgp.population.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Luiz Otavio Vilas Boas Oliveira
 * http://homepages.dcc.ufmg.br/~luizvbo/ 
 * luiz.vbo@gmail.com
 * Copyright (C) 2014, Federal University of Minas Gerais, Belo Horizonte, Brazil
 */

public class GSGP {
    private final PropertiesManager properties;
    private final Statistics statistics;
    private final ExperimentalData expData;
    private final MersenneTwister rndGenerator;


    public GSGP(PropertiesManager properties, ExperimentalData expData) throws Exception{
        this.properties = properties;
        this.expData = expData;
        statistics = new Statistics(properties.getNumGenerations(), expData);
        rndGenerator = properties.getRandomGenerator();
    }


    public void evolve() throws Exception{
        statistics.startClock();

        // Early stopping flag
        boolean canStop = false;

        // Initialize auxiliary structures
        Populator populator = properties.getPopulationInitializer();
        Pipeline pipe = properties.getPipeline();
        pipe.setup(properties, statistics, expData, rndGenerator);

        // Generate initial population
        Population population = populator.populate(rndGenerator, expData, properties.getPopulationSize());

        statistics.addGenerationStatistic(population);

        // Maps between Objects and hashCodes used in individual reconstruction
        Map<Integer, Individual> initialInds = new HashMap<>();
        Map<Integer, Node> mutationMasks = new HashMap<>();

        // Maps initial individuals' Object to hashCode
        for(Individual ind : population) {
            Integer indHash = ind.hashCode();
            initialInds.put(indHash, ind);
        }
        
        for(int i = 0; i < properties.getNumGenerations() && !canStop; i++){
            // Evolve a new Population
            Population newPopulation = pipe.evolvePopulation(population, expData, properties.getPopulationSize()-1, mutationMasks);

            // The first position is reserved for the best of the generation (elitism)
            newPopulation.add(population.getBestIndividual());

            // Check stopping criterion
            Individual bestIndividual = newPopulation.getBestIndividual();
            if(bestIndividual.isBestSolution(properties.getMinError())) canStop = true;

            // Update the population
            population = newPopulation;

            // Save statistics to file
            statistics.addGenerationStatistic(population);
        }


        // Save best individual's statistics to file
        statistics.finishEvolution(population.getBestIndividual());

        /*
        // Reconstruct best individual
        Node reconstructedTree = reconstructIndividual(population.get(0), initialInds, mutationMasks);
        Fitness fitnessFunction = reconstructedTree.evaluateFitness(expData, properties);
        GSGPIndividual reconstructedInd = new GSGPIndividual(reconstructedTree, fitnessFunction);

        // Print equivalent trees' size for comparison
        System.out.println("Best Individual Size: " + ((GSGPIndividual) population.getBestIndividual()).getNumNodes());
        System.out.println("Reconstruction Size: " + reconstructedInd.getTree().getNumNodes() + "\n");;
        System.out.println("Best Individual TR Fitness: " + population.getBestIndividual().getTrainingFitnessAsString());
        System.out.println("Best Individual TS Fitness: " + population.getBestIndividual().getTestFitnessAsString());
        System.out.println("Reconstruction TR Fitness: " + reconstructedInd.getTrainingFitnessAsString());
        System.out.println("Reconstruction TS Fitness: " + reconstructedInd.getTestFitnessAsString() + "\n");

        /******* EXTRA DATA *******

        // Print sizes in scientific notation
        NumberFormat formatter = new DecimalFormat("0.###E0");

        // Print trees features
        System.out.println("Best Individual Size: " + formatter.format(((GSGPIndividual) population.getBestIndividual()).getNumNodes()));
        System.out.println("Best Individual TR Fitness: " + population.getBestIndividual().getTrainingFitnessAsString());
        System.out.println("Best Individual TS Fitness: " + population.getBestIndividual().getTestFitnessAsString());
        System.out.println("---------------------------------------------");
        System.out.println("Reconstruction Size: " + formatter.format(reconstructedInd.getTree().getNumNodes()));
        System.out.println("Reconstruction TR Fitness: " + reconstructedInd.getTrainingFitnessAsString());
        System.out.println("Reconstruction TS Fitness: " + reconstructedInd.getTestFitnessAsString() + "\n");

        // Print reconstructed tree representations
        System.out.println(reconstructedTree);
        System.out.println(((GSGPIndividual) population.getBestIndividual()).getReprCoef() + "\n");

         **************************/
    }


    /**
     * Get statistics for current GSGP instance.
     *
     * @return
     */
    public Statistics getStatistics() {
        return statistics;
    }


    /**
     * Reconstruct an equivalent tree based in the individual's coefficient representation.
     *
     * @param individual
     * @param initialPop
     * @param mutationMasks
     * @return
     */
    public Node reconstructIndividual(Individual individual, Map initialPop, Map mutationMasks) {
        // Maps of coefficients related to each subtree
        HashMap<Integer, Double> reprCoef = (HashMap<Integer, Double>) ((GSGPIndividual) individual).getReprCoef();

        // Root node
        Add root = new Add();

        Function current = root;

        // Iterate over subtrees
        for(Map.Entry<Integer, Double> entry : reprCoef.entrySet()) {
            // Multiplication to apply coefficient to subtree
            Mul applyCoef = new Mul();

            // Include coefficient as a subnode of the multiplication
            applyCoef.addNode(new ERC(entry.getValue()), 0);

            // Get and attach subtree root node
            Individual subInd = (Individual) initialPop.get(entry.getKey());
            if(subInd == null) {  // Subtree is a mutation mask
                applyCoef.addNode((Node) mutationMasks.get(entry.getKey()), 1);
            }
            else {  // Subtree is a parent individual
                applyCoef.addNode(subInd.getTree(), 1);
            }

            // Attach new term to the main tree
            current.addNode(applyCoef, 0);

            // Addition operation necessary to continue the chain of terms
            Add nextAdd = new Add();
            current.addNode(nextAdd, 1);

            current = nextAdd;
        }

        // Creates and addition of two zeros so a Function is not a leaf node
        current.addNode(new ERC(0), 0);
        current.addNode(new ERC(0), 1);

        return root;
    }
}
