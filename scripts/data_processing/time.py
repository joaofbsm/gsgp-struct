#!/usr/bin/env python3

"""Calculate the mean and standard deviation of the time results"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "MIT"

import os
import sys
import numpy as np

np.set_printoptions(precision=4)

datasets = ["airfoil", "ccn", "ccun", "concrete", "energyCooling", "energyHeating", "parkinsons", "ppb-wth0s", "towerData", "wineRed", "wineWhite", "yacht"]

# GP
input_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/gp/out_files/"
output_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/results/gp/time/"

# GSGP CANONICAL
#input_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/gsgp-canonical/"
#output_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/results/canonical/time/"

# GSGP CP
#input_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/gsgp(hydra)/"
#output_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/results/gsgp/time/"

file_names = ["elapsedTime.csv"]


def main(args):
    for dataset in datasets:
        actual_path = input_path + "output-" + dataset + "/"

        for file_name in file_names:
            data = np.genfromtxt(actual_path + file_name, delimiter=',')
            data = data[:, -1]

            mean = np.mean(data, axis=0)
            stddev = np.std(data, axis=0)

            with open(output_path + dataset + ".csv", 'w') as f:
                f.write("{:.3f}({:.3f})".format(mean, stddev))
                f.write("\n")

if __name__ == "__main__":
    main(sys.argv)