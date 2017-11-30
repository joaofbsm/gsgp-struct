#!/bin/bash
#title          :execute.sh
#description    :Execute GSGP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :17.10.2017
#usage          :bash execute.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#==============================================================================

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
#datasets=("yacht")
gsgp_path=$(pwd)"/gsgp-canonical/out/artifacts/gsgp_jar"
experiments_path=$(pwd)"/experiments"
results_path=$(pwd)"/results-canonical"
scripts_path=$(pwd)"/scripts"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -jar "$gsgp_path"/gsgp.jar -p "$experiments_path"/"$dataset".txt > "$results_path"/"$dataset".txt
    #python3 "$scripts_path"/plot_freq.py -d "$dataset"
    #python3 "$scripts_path"/plot_ind.py -d "$dataset"
done