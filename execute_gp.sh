#!/bin/bash
#title          :execute_gp.sh
#description    :Execute GP for all specified datasets
#author         :Joao Francisco B. S. Martins
#date           :30.11.2017
#usage          :bash execute_gp.sh
#bash_version   :GNU bash, version 4.4.0(1)-release
#==============================================================================

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb" "towerData" "wineRed" "wineWhite" "yacht")
num_input_attrib=("10" "127" "129" "13" "13" "13" "23" "259" "30" "16" "16" "11")
gp_path=$(pwd)"/gp"
datasets_path=$(pwd)"/datasets"
results_path=$(pwd)"/results/gp"
scripts_path=$(pwd)"/scripts"

mkdir -p "$results_path"

for ((i=0; i<"${#datasets[@]}"; i++));
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -cp "$gp_path"/GP.jar ec.gp.core.Experimenter -o "$results_path"/out_files -t 4 -n 1 -B -p "$gp_path"/parameters/gp-with-AQ.params -i "$datasets_path"/"${datasets[$i]}"-#.csv -a output-"${datasets[$i]}" -g 1 -P generations=250 -P pop.subpop.0.size=1000 -P select.tournament.size=7 -P gp.fs.0.size="${num_input_attrib[$i]}" -nf 1 -s 123456 > "$results_path"/"${datasets[$i]}".txt
done

python3 "$scripts_path"/combine_gp_out.py