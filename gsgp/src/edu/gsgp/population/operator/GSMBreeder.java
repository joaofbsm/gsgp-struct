/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp.population.operator;

import edu.gsgp.MersenneTwister;
import edu.gsgp.Utils;
import edu.gsgp.Utils.DatasetType;
import edu.gsgp.data.Dataset;
import edu.gsgp.data.ExperimentalData;
import edu.gsgp.data.Instance;
import edu.gsgp.data.PropertiesManager;
import edu.gsgp.nodes.Node;
import edu.gsgp.population.GSGPIndividual;
import edu.gsgp.population.Individual;
import edu.gsgp.population.fitness.Fitness;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author Luiz Otavio Vilas Boas Oliveira
 * http://homepages.dcc.ufmg.br/~luizvbo/ 
 * luiz.vbo@gmail.com
 * Copyright (C) 20014, Federal University of Minas Gerais, Belo Horizonte, Brazil
 */
public class GSMBreeder extends Breeder{

    public GSMBreeder(PropertiesManager properties, Double probability) {
        super(properties, probability);
    }

    private Fitness evaluate(GSGPIndividual ind,
                             Node randomTree1,
                             Node randomTree2,
                             ExperimentalData expData){
        Fitness fitnessFunction = ind.getFitnessFunction().softClone();
        for(DatasetType dataType : DatasetType.values()){
            // Compute the (training/test) semantics of generated random tree
            fitnessFunction.resetFitness(dataType, expData);
            Dataset dataset = expData.getDataset(dataType);
            double[] semInd;
            if(dataType == DatasetType.TRAINING)
                semInd = ind.getTrainingSemantics();
            else
                semInd =  ind.getTestSemantics();
            int instanceIndex = 0;
            for (Instance instance : dataset) {
                //double rtValue = Utils.sigmoid(randomTree1.eval(instance.input));
                double rtValue = randomTree1.eval(instance.input);
                //rtValue -= Utils.sigmoid(randomTree2.eval(instance.input));
                rtValue -= randomTree2.eval(instance.input);
                double estimated = semInd[instanceIndex] + properties.getMutationStep() * rtValue;
                fitnessFunction.setSemanticsAtIndex(estimated, instance.output, instanceIndex++, dataType);
            }
            fitnessFunction.computeFitness(dataType);
        }
        return fitnessFunction;
    }

    @Override
    public Individual generateIndividual(MersenneTwister rndGenerator, ExperimentalData expData) {
        GSGPIndividual p = (GSGPIndividual)properties.selectIndividual(originalPopulation, rndGenerator);
        Node rt1 = properties.getRandomTree(rndGenerator);
        Node rt2 = properties.getRandomTree(rndGenerator);
        BigInteger numNodes = p.getNumNodes().add(new BigInteger(rt1.getNumNodes()+"")).
                add(new BigInteger(rt2.getNumNodes()+"")).
                add(BigInteger.ONE);
        Fitness fitnessFunction = evaluate(p, rt1, rt2, expData);
        GSGPIndividual offspring = new GSGPIndividual(numNodes, fitnessFunction, p, null, null, rt1, rt2, properties.getMutationStep());
        return offspring;
    }


    public Individual generateIndividual(MersenneTwister rndGenerator, ExperimentalData expData, Map mutationMasks) {
        GSGPIndividual p = (GSGPIndividual)properties.selectIndividual(originalPopulation, rndGenerator);
        Node rt1 = properties.getRandomTree(rndGenerator);
        Node rt2 = properties.getRandomTree(rndGenerator);
        BigInteger numNodes = p.getNumNodes().add(new BigInteger(rt1.getNumNodes()+"")).
                add(new BigInteger(rt2.getNumNodes()+"")).
                add(BigInteger.ONE);
        Fitness fitnessFunction = evaluate(p, rt1, rt2, expData);
        GSGPIndividual offspring = new GSGPIndividual(numNodes, fitnessFunction, p, null, null, rt1, rt2, properties.getMutationStep());
        mutationMasks.put(rt1.toString().hashCode(), rt1);
        mutationMasks.put(rt2.toString().hashCode(), rt2);
        return offspring;
    }

    @Override
    public Breeder softClone(PropertiesManager properties) {
        return new GSMBreeder(properties, probability);
    }
}