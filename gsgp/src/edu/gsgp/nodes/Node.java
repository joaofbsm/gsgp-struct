/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp.nodes;

import edu.gsgp.Utils;
import edu.gsgp.data.Dataset;
import edu.gsgp.data.ExperimentalData;
import edu.gsgp.data.Instance;
import edu.gsgp.data.PropertiesManager;
import edu.gsgp.population.fitness.Fitness;

/**
 * @author Luiz Otavio Vilas Boas Oliveira
 * http://homepages.dcc.ufmg.br/~luizvbo/ 
 * luiz.vbo@gmail.com
 * Copyright (C) 2014, Federal University of Minas Gerais, Belo Horizonte, Brazil
 */
public interface Node {
    public int getArity();
    
    public double eval(double[] inputs);
    
    public int getNumNodes();
    
    public Node clone(Node parent);
    
    public Node getChild(int index);
    
    public Node getParent();
    
    public void setParent(Node parent, int argPosition);
    
    public int getParentArgPosition();

    /**
     * Default method to evaluate the fitness of a tree beginning at this node.
     *
     * @param expData
     * @param properties
     * @return
     */
    default public Fitness evaluateFitness(ExperimentalData expData, PropertiesManager properties){
        Fitness fitnessFunction = properties.getFitnessFunction();

        // Compute the (training/test) semantics of generated random tree
        for(Utils.DatasetType dataType : Utils.DatasetType.values()){
            fitnessFunction.resetFitness(dataType, expData);
            Dataset dataset = expData.getDataset(dataType);

            int instanceIndex = 0;
            for (Instance instance : dataset) {
                double estimated = this.eval(instance.input);
                fitnessFunction.setSemanticsAtIndex(estimated, instance.output, instanceIndex++, dataType);
            }

            fitnessFunction.computeFitness(dataType);
        }
        return fitnessFunction;
    }
}
