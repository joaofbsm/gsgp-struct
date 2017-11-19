/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp;

import edu.gsgp.data.ExperimentalData;
import edu.gsgp.population.Population;
import edu.gsgp.population.Individual;
import edu.gsgp.data.PropertiesManager;
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
 * Copyright (C) 20014, Federal University of Minas Gerais, Belo Horizonte, Brazil
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
        boolean canStop = false;     
        
        Populator populator = properties.getPopulationInitializer();
        Pipeline pipe = properties.getPipeline();
        
        statistics.startClock();
        
        Population population = populator.populate(rndGenerator, expData, properties.getPopulationSize());
        pipe.setup(properties, statistics, expData, rndGenerator);
        
        statistics.addGenerationStatistic(population);

        Population initialPopulation = population;

        Map<Integer, Individual> indMap = new HashMap<>();
        Map<Integer, BigInteger> freqMap = new HashMap<>();
        Map<Integer, HashMap<Integer, BigInteger>> reprMap = new HashMap<>();

        for(Individual ind : population) {
            Integer indHash = ind.hashCode();
            indMap.put(indHash, ind);
            freqMap.put(indHash, BigInteger.valueOf(0));
        }
        
        for(int i = 0; i < properties.getNumGenerations() && !canStop; i++){
            //System.out.println("Generation " + (i+1) + ":");
                        
            // Evolve a new Population
            Population newPopulation = pipe.evolvePopulation(population, expData, properties.getPopulationSize()-1);
            // The first position is reserved for the best of the generation (elitism)
            newPopulation.add(population.getBestIndividual());
            Individual bestIndividual = newPopulation.getBestIndividual();
            if(bestIndividual.isBestSolution(properties.getMinError())) canStop = true;
            population = newPopulation;

            for(Individual ind : population) {
                getReprFreq(ind, freqMap, reprMap);
            }


            statistics.addGenerationStatistic(population);
        }

        Map<Integer, BigInteger> sortedFreqMap = freqMap.entrySet()
                                                        .stream()
                                                        .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                                                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                                                  Map.Entry::getValue,
                                                                                  (e1, e2) -> e1,
                                                                                  LinkedHashMap::new));

        saveInds(indMap, sortedFreqMap, properties);
        saveReprs(population, reprMap, properties);

        System.out.println(sortedFreqMap);
        printPopFitness(initialPopulation);
        statistics.finishEvolution(population.getBestIndividual());
    }

    public Statistics getStatistics() {
        return statistics;
    }


    /**
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
     *
     * @param reprMap
     * @param ind
     */
    public void addRepr(Map reprMap, Individual ind) {
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
     *
     * @param indMap
     * @param sortedFreqMap
     * @param properties
     * @throws IOException
     */
    public void saveInds(Map indMap, Map sortedFreqMap, PropertiesManager properties) throws IOException {
        BigInteger threshold = (BigInteger.valueOf((int) java.lang.Math.pow(10, 20)));

        Iterator it = sortedFreqMap.entrySet().iterator();

        int i = 0;
        Map.Entry first = (Map.Entry) it.next();

        Integer indHash = (Integer) first.getKey();
        BigInteger lastFreq = (BigInteger) first.getValue();
        Individual ind = (Individual) indMap.get(indHash);

        saveInd(ind.toString(), i, lastFreq, properties);
        i += 1;

        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();

            indHash = (Integer) entry.getKey();
            BigInteger freq = (BigInteger) entry.getValue();

            // freq is bigger than the threshold
            if (lastFreq.divide(threshold).compareTo(freq) == -1) {
                ind = (Individual) indMap.get(indHash);
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
     *
     * @param population
     * @param reprMap
     * @param properties
     */
    public void saveReprs(Population population, Map reprMap, PropertiesManager properties) throws IOException {
        File out_dir = new File(properties.getOutputDir() + File.separator + properties.getFilePrefix() + File.separator);
        out_dir.mkdirs();

        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(out_dir.getAbsolutePath() + File.separator + "lastGenRepr.txt", false));

        for(Individual ind : population) {
            addRepr(reprMap, ind);

            Integer indHash = ind.hashCode();
            HashMap<Integer, BigInteger> repr = (HashMap<Integer, BigInteger>) reprMap.get(indHash);

            Map<Integer, BigInteger> sortedRepr = repr.entrySet()
                                                      .stream()
                                                      .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                                                                Map.Entry::getValue,
                                                                                (e1, e2) -> e1,
                                                                                LinkedHashMap::new));

            if(repr != null) {
                bw.write("[" + indHash + "(TR RMSE: " + ind.getTrainingFitnessAsString() + ") -> " +  sortedRepr.toString() + "]\n");
            }
        }

        bw.close();
    }


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

}
