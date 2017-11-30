#!/bin/bash
#title          :execute_gp.sh
#description    :Execute GP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :30.11.2017
#usage          :bash execute_gp.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#==============================================================================

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
gp_path=$(pwd)"/gp"
datasets_path=$(pwd)"/datasets/original"
results_path=$(pwd)"/results/gp"
scripts_path=$(pwd)"/scripts"

mkdir -p "$results_path"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -cp "$gp_path"/GP.jar ec.gp.core.Experimenter -o "$results_path"/out_files -t 4 -n 1 -B -p "$gp_path"/parameters/gp-with-AQ.params -i "$datasets_path"/"$dataset"-#.csv -a output-"$dataset" -g 1 -P generations=250 -P pop.subpop.0.size=1000 -P select.tournament.size=10 -P gp.fs.0.size=13 -nf 1 -s 123456 > "$results_path"/"$dataset".txt
done

python3 "$scripts_path"/combine_gp_out.py