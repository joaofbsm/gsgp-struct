#!/bin/bash
#title          :execute_gsgp.sh
#description    :Execute GSGP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :17.10.2017
#usage          :bash execute_gsgp.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#==============================================================================

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
#datasets=("yacht")
gsgp_path=$(pwd)"/gsgp/out/artifacts/gsgp_jar"
experiments_path=$(pwd)"/experiments/gsgp"
results_path=$(pwd)"/results/gsgp"
scripts_path=$(pwd)"/scripts"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -jar "$gsgp_path"/gsgp.jar -p "$experiments_path"/"$dataset".txt > "$results_path"/"$dataset".txt
    #python3 "$scripts_path"/plot_freq.py -d "$dataset"
    #python3 "$scripts_path"/plot_ind.py -d "$dataset"
done