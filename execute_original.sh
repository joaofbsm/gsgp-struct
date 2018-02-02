#!/bin/bash
#title          :execute_original.sh
#description    :Execute GSGP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :26.01.2017
#usage          :bash execute_original.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#==============================================================================

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb" "towerData" "wineRed" "wineWhite" "yacht")
gsgp_path=$(pwd)"/gsgp-original/out/artifacts/gsgp_jar"
experiments_path=$(pwd)"/experiments/gsgp-original"
results_path=$(pwd)"/results/gsgp-original"
scripts_path=$(pwd)"/scripts"

mkdir -p "$results_path"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -jar "$gsgp_path"/gsgp.jar -p "$experiments_path"/"$dataset".txt > "$results_path"/"$dataset".txt
done