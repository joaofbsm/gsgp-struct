/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp.population;

import edu.gsgp.GSGP;
import edu.gsgp.Utils;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.gsgp.nodes.Node;
import edu.gsgp.population.fitness.Fitness;
import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * @author Luiz Otavio Vilas Boas Oliveira
 * http://homepages.dcc.ufmg.br/~luizvbo/ 
 * luiz.vbo@gmail.com
 * Copyright (C) 20014, Federal University of Minas Gerais, Belo Horizonte, Brazil
 */
public class GSGPIndividual extends Individual{

    private Double crossoverConst;
    private Node mutationT1;
    private Node mutationT2;
    private double mutationStep;
    private Map<Integer, Double> reprCoef;

    public GSGPIndividual(Node tree, Fitness fitnessFunction){
        super(tree, fitnessFunction);
    }

    public GSGPIndividual(Fitness fitnessFunction){
        super(null, fitnessFunction);
    }

    public GSGPIndividual(Node tree, BigInteger numNodes, Fitness fitnessFunction) {
        super(tree, fitnessFunction);
        fitnessFunction.setNumNodes(numNodes);
        this.crossoverConst = null;
        this.mutationT1 = null;
        this.mutationT2 = null;
        this.mutationStep = 0;

        reprCoef = new HashMap<>();
        reprCoef.put(this.hashCode(), 1.0);
    }

    public GSGPIndividual(Node tree, int numNodes, Fitness fitnessFunction) {
        this(tree, new BigInteger(numNodes + ""), fitnessFunction);
    }

    public GSGPIndividual(BigInteger numNodes, Fitness fitnessFunction) {
        this(null, numNodes, fitnessFunction);
    }

    public GSGPIndividual(BigInteger numNodes,  Fitness fitnessFunction, GSGPIndividual T1, GSGPIndividual T2, Double crossoverConst, Node mutationT1, Node mutationT2, double mutationStep) {
        super(null, fitnessFunction, T1, T2);
        fitnessFunction.setNumNodes(numNodes);
        this.crossoverConst = crossoverConst;
        this.mutationT1 = mutationT1;
        this.mutationT2 = mutationT2;
        this.mutationStep = mutationStep;

        //System.out.println(crossoverConst);
        //System.out.println(mutationStep);

        reprCoef = new HashMap<>();
        this.propagateCoefficients();
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

    public void propagateCoefficients() {
        if(this.crossoverConst != null) {  // Crossover offspring
            GSGPIndividual parent1 = (GSGPIndividual) this.getParent1();
            this.addCoefficients(parent1.getReprCoef(), this.crossoverConst);

            GSGPIndividual parent2 = (GSGPIndividual) this.getParent2();
            this.addCoefficients(parent2.getReprCoef(), (1.0 - this.crossoverConst));
        }

        else {  // Mutation offspring
            GSGPIndividual parent1 = (GSGPIndividual) this.getParent1();
            this.addCoefficients(parent1.getReprCoef(), 1.0);

            Integer t1Hash = this.mutationT1.hashCode();
            Integer t2Hash = this.mutationT2.hashCode();

            Double storedCoef = this.reprCoef.get(t1Hash);
            this.reprCoef.put(this.mutationT1.toString().hashCode(), (storedCoef == null) ? this.mutationStep : storedCoef + this.mutationStep);

            storedCoef = this.reprCoef.get(t2Hash);
            this.reprCoef.put(this.mutationT2.toString().hashCode(), (storedCoef == null) ? (this.mutationStep * -1) : storedCoef + (this.mutationStep * -1));
        }
    }


    public void addCoefficients(Map parentRepr, double multiplyBy) {
        Iterator it = parentRepr.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();

            Integer indHash = (Integer) entry.getKey();
            Double coef = (Double) entry.getValue();
            Double storedCoef = this.reprCoef.get(indHash);

            coef = coef * multiplyBy;

            this.reprCoef.put(indHash, (storedCoef == null) ? coef : storedCoef + coef);
        }
    }
}
