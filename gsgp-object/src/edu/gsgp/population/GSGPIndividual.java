/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp.population;

import edu.gsgp.Utils;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import edu.gsgp.nodes.Node;
import edu.gsgp.population.fitness.Fitness;

/**
 * @author Luiz Otavio Vilas Boas Oliveira
 * http://homepages.dcc.ufmg.br/~luizvbo/ 
 * luiz.vbo@gmail.com
 * Copyright (C) 2014, Federal University of Minas Gerais, Belo Horizonte, Brazil
 */
public class GSGPIndividual extends Individual{

    private Map<Node, Double> reprCoef;
        
    public GSGPIndividual(Node tree, Fitness fitnessFunction){
        super(tree, fitnessFunction);
    }
    
    public GSGPIndividual(Fitness fitnessFunction){
        super(null, fitnessFunction);
    }
    
    public GSGPIndividual(Node tree, BigInteger numNodes, Fitness fitnessFunction) {
        super(tree, fitnessFunction);
        fitnessFunction.setNumNodes(numNodes);

        // Individual belongs to the initial population
        reprCoef = new HashMap<>();
        reprCoef.put(tree, 1.0);
    }

    public GSGPIndividual(BigInteger numNodes,  Fitness fitnessFunction, GSGPIndividual T1, GSGPIndividual T2, Double crossoverConst, Node mutationMask1, Node mutationMask2, double mutationStep) {
        super(null, fitnessFunction);

        fitnessFunction.setNumNodes(numNodes);

        reprCoef = new HashMap<>();
        this.propagateCoefficients(T1, T2, crossoverConst, mutationMask1, mutationMask2, mutationStep);
    }
    
    public GSGPIndividual(Node tree, int numNodes, Fitness fitnessFunction) {
        this(tree, new BigInteger(numNodes + ""), fitnessFunction);
    }
    
    public GSGPIndividual(BigInteger numNodes, Fitness fitnessFunction) {
        this(null, numNodes, fitnessFunction);
    }
    
    public double eval(double[] input){
        return tree.eval(input);
    }

    public BigInteger getNumNodes() {
        return fitnessFunction.getNumNodes();
    }

    public void setNumNodes(BigInteger numNodes) {
        fitnessFunction.setNumNodes(numNodes);
    }
   
    public void startNumNodes() {
        fitnessFunction.setNumNodes(tree.getNumNodes());
    }
    
    @Override
    public GSGPIndividual clone(){
        if(tree != null)
            return new GSGPIndividual(tree.clone(null), fitnessFunction);
        return new GSGPIndividual(fitnessFunction);
    }

    @Override
    public boolean isBestSolution(double minError) {
        return getFitness() <= minError;
    }

    @Override
    public String toString() {
        return tree.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTree(Node randomSubtree) {
        this.tree = randomSubtree;
    }

    @Override
    public String getNumNodesAsString() {
        return fitnessFunction.getNumNodes().toString();
    }

    @Override
    public String getTrainingFitnessAsString() {
        return Utils.format(fitnessFunction.getTrainingFitness());
    }

    @Override
    public String getTestFitnessAsString() {
        return Utils.format(fitnessFunction.getTestFitness());
    }

    @Override
    public double getFitness() {
        double value = fitnessFunction.getComparableValue();
        if(Double.isInfinite(value) || Double.isNaN(value)) return Double.MAX_VALUE;
        return value;
    }

    @Override
    public double[] getTrainingSemantics() {
        return fitnessFunction.getSemantics(Utils.DatasetType.TRAINING);
    }

    @Override
    public double[] getTestSemantics() {
        return fitnessFunction.getSemantics(Utils.DatasetType.TEST);
    }


    public Map getReprCoef(){
        return this.reprCoef;
    }


    /**
     * Propagate coefficients from individual's "recipe".
     *
     * @param parent1
     * @param parent2
     */
    public void propagateCoefficients(GSGPIndividual parent1, GSGPIndividual parent2, Double crossoverConst, Node mutationMask1, Node mutationMask2, double mutationStep) {
        if(crossoverConst != null) {  // Crossover offspring
            this.addCoefficients(parent1.getReprCoef(), crossoverConst);

            this.addCoefficients(parent2.getReprCoef(), (1.0 - crossoverConst));
        }

        else {  // Mutation offspring
            this.addCoefficients(parent1.getReprCoef(), 1.0);

            Double storedCoef = this.reprCoef.get(mutationMask1);
            this.reprCoef.put(mutationMask1, (storedCoef == null) ? mutationStep : storedCoef + mutationStep);

            storedCoef = this.reprCoef.get(mutationMask2);
            this.reprCoef.put(mutationMask2, (storedCoef == null) ? (mutationStep * -1) : storedCoef + (mutationStep * -1));
        }
    }


    /**
     * Add coefficients from a parent's representation times a constant to this individual representation.
     *
     * @param parentRepr
     * @param multiplyBy
     */
    public void addCoefficients(Map parentRepr, double multiplyBy) {
        for(Map.Entry<Node, Double> entry : ((HashMap<Node, Double>) parentRepr).entrySet()) {
            Node indRoot = (Node) entry.getKey();
            Double coef = (Double) entry.getValue();
            Double storedCoef = this.reprCoef.get(indRoot);

            coef = coef * multiplyBy;

            this.reprCoef.put(indRoot, (storedCoef == null) ? coef : storedCoef + coef);
        }
    }
}
