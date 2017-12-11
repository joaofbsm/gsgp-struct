/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp;

import edu.gsgp.data.Dataset;
import edu.gsgp.data.ExperimentalData;
import edu.gsgp.data.Instance;
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

        // Reconstruct best individual
        Node reconstructedTree = reconstructIndividual(population.get(0), initialInds, mutationMasks);
        Fitness fitnessFunction = reconstructedTree.evaluateFitness(expData, properties);
        GSGPIndividual reconstructedInd = new GSGPIndividual(reconstructedTree, fitnessFunction);

        // Print equivalent trees' size for comparison
        System.out.println("Best Individual Size: " + ((GSGPIndividual) population.getBestIndividual()).getNumNodes());
        System.out.println("Reconstruction Size: " + reconstructedInd.getTree().getTreeSize() + "\n");;

        /******* EXTRA DATA *******

        // Print sizes in scientific notation
        NumberFormat formatter = new DecimalFormat("0.###E0");

        // Print trees features
        System.out.println("Best Individual Size: " + formatter.format(((GSGPIndividual) population.getBestIndividual()).getNumNodes()));
        System.out.println("Best Individual TR Fitness: " + population.getBestIndividual().getTrainingFitnessAsString());
        System.out.println("Best Individual TS Fitness: " + population.getBestIndividual().getTestFitnessAsString());
        System.out.println("---------------------------------------------");
        System.out.println("Reconstruction Size: " + formatter.format(newInd.getTree().getTreeSize()));
        System.out.println("Reconstruction TR Fitness: " + newInd.getTrainingFitnessAsString());
        System.out.println("Reconstruction TS Fitness: " + newInd.getTestFitnessAsString() + "\n");

        // Print reconstructed tree representations
        System.out.println(reconstructedTree);
        System.out.println(((GSGPIndividual) population.getBestIndividual()).getReprCoef() + "\n");

         **************************/

        // Save best individual's statistics to file
        statistics.finishEvolution(population.getBestIndividual());
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
     * Get the individual representation based in the frequency of trees in its parents' representation.
     *
     * @param ind
     * @param freqMap
     * @param reprMap
     */
    public void getReprFreq(Individual ind, Map freqMap, Map reprMap) {
        Individual parent1 = ind.getParent1();
        Individual parent2 = ind.getParent2();

        addInd(parent1, freqMap, reprMap);
        addInd(parent2, freqMap, reprMap);
    }


    /**
     * Include individual representation to reprMap if not present and add its representation frequencies to the total.
     *
     * @param ind
     * @param freqMap
     * @param reprMap
     */
    public void addInd(Individual ind, Map freqMap, Map reprMap) {
        if (ind != null) {
            Integer indHash = ind.hashCode();
            HashMap<Integer, BigInteger> repr = (HashMap<Integer, BigInteger>) reprMap.get(indHash);

            if (repr == null) {
                addRepr(reprMap, ind);
                repr = (HashMap<Integer, BigInteger>) reprMap.get(indHash);
            }

            addFreq(freqMap, repr);
        }
    }


    /**
     * Links individual's frequency map and its hashCode into a representation map.
     *
     * @param reprMap
     * @param ind
     */
    public void addRepr(Map reprMap, Individual ind) {
        // This is a freqMap for the individual representation, not the global freqMap
        Map<Integer, BigInteger> freqMap = new HashMap<>();

        Individual parent1 = ind.getParent1();
        Individual parent2 = ind.getParent2();

        Integer indHash = ind.hashCode();

        reprMap.put(indHash, freqMap);
        if ((parent1 == null) && (parent2 == null)) {
            freqMap.put(indHash, BigInteger.valueOf(1));
        }
        else {
            if (parent1 != null) {
                HashMap<Integer, BigInteger> parent1Repr = (HashMap<Integer, BigInteger>) reprMap.get(parent1.hashCode());
                addFreq(freqMap, parent1Repr);
            }

            if (parent2 != null) {
                HashMap<Integer, BigInteger> parent2Repr = (HashMap<Integer, BigInteger>) reprMap.get(parent2.hashCode());
                addFreq(freqMap, parent2Repr);
            }
        }
    }


    /**
     * Adds the frequency of an individual's representation to a given frequency map.
     *
     * @param freqMap
     * @param repr
     */
    public void addFreq(Map freqMap, Map repr) {
        Iterator it = repr.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();

            Integer indHash = (Integer) entry.getKey();
            BigInteger freq = (BigInteger) entry.getValue();
            BigInteger storedFreq = (BigInteger) freqMap.get(indHash);

            freqMap.put(indHash, (storedFreq == null) ? freq : storedFreq.add(freq));
        }
    }


    /**
     * Save to file all the initial individuals' tree toString representation and its frequency of appearance.
     *
     * @param initialInds
     * @param sortedFreqMap
     * @param properties
     * @throws IOException
     */
    public void saveInds(Map initialInds, Map sortedFreqMap, PropertiesManager properties) throws IOException {
        BigInteger threshold = (BigInteger.valueOf((int) java.lang.Math.pow(10, 20)));

        Iterator it = sortedFreqMap.entrySet().iterator();

        int i = 0;
        Map.Entry first = (Map.Entry) it.next();

        Integer indHash = (Integer) first.getKey();
        BigInteger lastFreq = (BigInteger) first.getValue();
        Individual ind = (Individual) initialInds.get(indHash);

        saveInd(ind.toString(), i, lastFreq, properties);
        i += 1;

        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();

            indHash = (Integer) entry.getKey();
            BigInteger freq = (BigInteger) entry.getValue();

            // freq is bigger than the threshold
            if (lastFreq.divide(threshold).compareTo(freq) == -1) {
                ind = (Individual) initialInds.get(indHash);
                saveInd(ind.toString(), i, freq, properties);

                lastFreq = freq;
                i += 1;
            }
            else {
                break;
            }

        }
    }


    /**
     * Save individual's tree toString representation and its frequency.
     *
     * @param ind
     * @param i
     * @param properties
     * @throws IOException
     */
    public void saveInd(String ind, int i, BigInteger freq, PropertiesManager properties) throws IOException {
        File out_dir = new File(properties.getOutputDir() + File.separator + properties.getFilePrefix() + "/individuals/");
        out_dir.mkdirs();

        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(out_dir.getAbsolutePath() + File.separator + Integer.toString(i) + ".txt", false));

        NumberFormat formatter = new DecimalFormat("0.#####E0");

        bw.write(formatter.format(freq));
        bw.write("\n");
        bw.write(ind);
        bw.close();
    }


    /**
     * Save to file the representation, based on the initial trees, of an entire population.
     *
     * @param population
     * @param reprMap
     * @param properties
     */
    public void saveReprs(int generation, Population population, Map reprMap, PropertiesManager properties) throws IOException {
        File out_dir = new File(properties.getOutputDir() + File.separator + properties.getFilePrefix() + File.separator + "repr" + File.separator);
        if(generation == 0) {
            out_dir.mkdirs();
        }

        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(out_dir.getAbsolutePath() + File.separator + Integer.toString(generation) + ".txt", false));

        for(Individual ind : population) {
            Integer indHash = ind.hashCode();
            HashMap<Integer, BigInteger> repr = (HashMap<Integer, BigInteger>) reprMap.get(indHash);

            if(repr == null) {
                addRepr(reprMap, ind);
                repr = (HashMap<Integer, BigInteger>) reprMap.get(indHash);
            }


            Map<Integer, BigInteger> sortedRepr = repr.entrySet()
                                                      .stream()
                                                      .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                                                                Map.Entry::getValue,
                                                                                (e1, e2) -> e1,
                                                                                LinkedHashMap::new));

            if(repr != null) {
                bw.write("[" + indHash + "(TR: " + ind.getTrainingFitnessAsString() + ", TS: " + ind.getTestFitnessAsString() + ") -> " +  sortedRepr.toString() + "]\n");
            }
        }

        bw.close();
    }


    /**
     * Print the fitness of the entire population in ascending order.
     *
     * @param population
     */
    public void printPopFitness(Population population) {
        Map<Integer, Double> rmseMap = new HashMap<>();

        for(Individual ind : population) {
            rmseMap.put(ind.hashCode(), Double.parseDouble(ind.getTrainingFitnessAsString()));
        }

        Map<Integer, Double> sortedRmse = rmseMap.entrySet()
                                                     .stream()
                                                     .sorted(Map.Entry.comparingByValue())
                                                     .collect(Collectors.toMap(Map.Entry::getKey,
                                                              Map.Entry::getValue,
                                                              (e1, e2) -> e1,
                                                              LinkedHashMap::new));

        System.out.println(sortedRmse);

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
        HashMap<Integer, Double> reprCoef = (HashMap<Integer, Double>) ((GSGPIndividual) individual).getReprCoef();

        Add root = new Add();

        Function current = root;

        for(Map.Entry<Integer, Double> entry : reprCoef.entrySet()) {
            Mul applyCoef = new Mul();
            current.addNode(applyCoef, 0);

            applyCoef.addNode(new ERC(entry.getValue()), 0);

            Individual subInd = (Individual) initialPop.get(entry.getKey());
            if(subInd == null) {
                applyCoef.addNode((Node) mutationMasks.get(entry.getKey()), 1);
            }
            else {
                applyCoef.addNode(subInd.getTree(), 1);
            }

            Add nextTerm = new Add();
            current.addNode(nextTerm, 1);
            current = nextTerm;
        }

        current.addNode(new ERC(0), 0);
        current.addNode(new ERC(0), 1);

        return root;
    }
}
