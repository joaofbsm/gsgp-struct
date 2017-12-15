#!/bin/bash
#title          :execute_canonical.sh
#description    :Execute Canonical GSGP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :30.11.2017
#usage          :bash execute.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#==============================================================================

#datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
datasets=("towerData")
gsgp_path=$(pwd)"/gsgp-canonical/out/artifacts/gsgp_jar"
experiments_path=$(pwd)"/experiments/gsgp-canonical"
results_path=$(pwd)"/results/gsgp-canonical"
scripts_path=$(pwd)"/scripts"

mkdir -p "$results_path"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -jar "$gsgp_path"/gsgp.jar -p "$experiments_path"/"$dataset"-canonical.txt > "$results_path"/"$dataset".txt
done

python3 "$scripts_path"/combine_canonical_out.py