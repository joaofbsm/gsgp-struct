#!/usr/bin/env python3

"""Calculate the mean and standard deviation of the size results"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "MIT"

import os
import sys
import numpy as np

np.set_printoptions(precision=4)

datasets = ["airfoil", "ccn", "ccun", "concrete", "energyCooling", "energyHeating", "parkinsons", "ppb-wth0s", "towerData", "wineRed", "wineWhite", "yacht"]

# GSGP CP
input_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/gsgp(hydra)/"
output_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/results/gsgp/size/"


def main(args):
    for dataset in datasets:
        best_size = np.empty(30)  # Number of repetitions
        reconstruct_size = np.empty(30)

        with open(input_path + dataset + ".txt", 'r') as f:
            lines = f.readlines()

            i = 0
            j = 0
            for line in lines:
                line = line.split(':')
                if line[0] == "Best Individual Size":
                    best_size[i] = line[1]
                    i += 1

                if line[0] == "Reconstruction Size":
                    reconstruct_size[j] = line[1]
                    j += 1

        with open(output_path + dataset + ".csv", 'w') as f:
            f.write("{:.4e}({:.4e})\n{:.4f}({:.4f})".format(np.mean(best_size, axis=0), np.std(best_size, axis=0), np.mean(reconstruct_size, axis=0), np.std(reconstruct_size, axis=0)))


if __name__ == "__main__":
    main(sys.argv)