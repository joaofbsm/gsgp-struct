/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp;

import edu.gsgp.data.ExperimentalData;
import edu.gsgp.nodes.Node;
import edu.gsgp.population.GSGPIndividual;
import edu.gsgp.population.Population;
import edu.gsgp.population.Individual;
import edu.gsgp.data.PropertiesManager;
import edu.gsgp.population.populator.Populator;
import edu.gsgp.population.pipeline.Pipeline;
import java.math.BigInteger;

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

        Map<Integer, BigInteger> freqMap = new HashMap<>();
        Map<Integer, HashMap<Integer, BigInteger>> reprMap = new HashMap<>();

        for(Individual ind : population) {
            Integer indHash = ind.hashCode();
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
                                                        .collect(Collectors.toMap(
                                                                Map.Entry::getKey,
                                                                Map.Entry::getValue,
                                                                (e1, e2) -> e1,
                                                                LinkedHashMap::new));

        System.out.println(sortedFreqMap);
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

        if (parent1 != null) {
            HashMap<Integer, BigInteger> parent1Repr = (HashMap<Integer, BigInteger>) reprMap.get(parent1.hashCode());
            addFreq(freqMap, parent1Repr);
        }

        if (parent2 != null) {
            HashMap<Integer, BigInteger> parent2Repr = (HashMap<Integer, BigInteger>) reprMap.get(parent2.hashCode());
            addFreq(freqMap, parent2Repr);
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
}
