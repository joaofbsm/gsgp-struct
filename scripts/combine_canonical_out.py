#!/usr/bin/env python3

"""Combines Canonical GSGP relevant output data in a single file"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "MIT"

import os
import sys

def main(args):
    dir_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results/gsgp-canonical/"

    open(dir_path + "results.txt", 'w').close()

    for file in sorted(os.listdir(dir_path)):
        if file.endswith(".txt") and not file == "results.txt":
            dataset = file.split('.')[0];
            with open(dir_path + file, 'r') as f:
                lines = f.readlines()
                if not lines:
                    continue
            bestTr = lines[-2].split(" ")[-1].split('/')[0]
            bestTs = lines[-2].split(" ")[-1].split('/')[1]

            with open(dir_path + "results.txt", 'a') as f:
                f.write("==> {} <==\n"
                        "Best Individual TR Fitness: {}\n"
                        "Best Individual TS Fitness: {}\n\n"
                        "{}\n".format(dataset, bestTr, bestTs, lines[-1]))


if __name__ == "__main__":
    main(sys.argv)
