#!/bin/bash
#title          :execute.sh
#description    :Execute GSGP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :17.10.2017
#usage          :bash execute.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#========================================================================

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
#datasets=("yacht")
gsgp_path="/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/gsgp/out/artifacts/gsgp_jar"
experiments_path="/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/experiments"
results_path="/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results"
scripts_path="/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/scripts"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -jar "$gsgp_path"/gsgp.jar -p "$experiments_path"/"$dataset".txt > "$results_path"/"$dataset".txt
    python3 "$scripts_path"/plot_freq.py -d "$dataset"
    python3 "$scripts_path"/plot_ind.py -d "$dataset"
done