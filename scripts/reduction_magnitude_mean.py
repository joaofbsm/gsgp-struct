#!/usr/bin/env python3

"""Calculate the mean of orders of magnitude reduced in the simplification process"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "MIT"

import os
import sys
import numpy as np

datasets = ["airfoil", "ccn", "ccun", "concrete", "energyCooling", "energyHeating", "parkinsons", "ppb-wth0s", "towerData", "wineRed", "wineWhite", "yacht"]

size_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/results/12-12-17-30repetitions/results/gsgp/size/"


def main(args):
    size_diff = np.empty(len(datasets))

    for i, dataset in enumerate(datasets):
        with open(size_path + dataset + ".csv", 'r') as f:
          sizes = f.readlines()
          
          #size_diff[i] = float(sizes[0].split("(")[0]) / float(sizes[1].split("(")[0])
          size_diff[i] = np.floor(np.log10(float(sizes[0].split("(")[0]))) - np.floor(np.log10(float(sizes[1].split("(")[0])))

    print(np.mean(size_diff))


if __name__ == "__main__":
    main(sys.argv)