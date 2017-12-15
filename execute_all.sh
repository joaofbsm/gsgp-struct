#######################
# GSGP RECONSTRUCTION #
#######################

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
#datasets=("yacht")
gsgp_path=$(pwd)"/gsgp/out/artifacts/gsgp_jar"
experiments_path=$(pwd)"/experiments/gsgp"
results_path=$(pwd)"/results/gsgp"
scripts_path=$(pwd)"/scripts"

mkdir -p "$results_path"

for dataset in "${datasets[@]}"
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -jar "$gsgp_path"/gsgp.jar -p "$experiments_path"/"$dataset".txt > "$results_path"/"$dataset".txt
    #python3 "$scripts_path"/plot_freq.py -d "$dataset"
    #python3 "$scripts_path"/plot_ind.py -d "$dataset"
done

tail -n 9 "$results_path"/*.txt > "$results_path"/results.txt

##################
# GSGP CANONICAL #
##################

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
#datasets=("airfoil")
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

######
# GP #
######

datasets=("airfoil" "ccn" "ccun" "concrete" "energyCooling" "energyHeating" "parkinsons" "ppb-wth0s" "towerData" "wineRed" "wineWhite" "yacht")
num_input_attrib=("10" "127" "129" "13" "13" "13" "23" "259" "30" "16" "16" "11")
gp_path=$(pwd)"/gp"
datasets_path=$(pwd)"/datasets/original"
results_path=$(pwd)"/results/gp"
scripts_path=$(pwd)"/scripts"

mkdir -p "$results_path"

for ((i=0; i<"${#datasets[@]}"; i++));
do
    echo "Executing $dataset"
    java -Xms512m -Xmx8g -cp "$gp_path"/GP.jar ec.gp.core.Experimenter -o "$results_path"/out_files -t 4 -n 50 -B -p "$gp_path"/parameters/gp-with-AQ.params -i "$datasets_path"/"${datasets[$i]}"-#.csv -a output-"${datasets[$i]}" -g 1 -P generations=250 -P pop.subpop.0.size=1000 -P select.tournament.size=7 -P gp.fs.0.size="${num_input_attrib[$i]}" -nf 1 -s 123456 > "$results_path"/"${datasets[$i]}".txt
done

python3 "$scripts_path"/combine_gp_out.py