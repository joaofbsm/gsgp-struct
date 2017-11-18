#!/usr/bin/env python3

"""Plot the frequency of appearence of initial population generated trees"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "GPL"
__version__ = "3.0"

import os
import argparse
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import rcParams

parser = argparse.ArgumentParser(description="Plotting Tree Frequency in GSGP")
parser.add_argument("-d", "--dataset")
args = parser.parse_args()

in_file = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results/{}.txt".format(args.dataset)
csv_file = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results/{}-formated.csv".format(args.dataset)
out_path = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results/output-{}/plots".format(args.dataset)
if not os.path.exists(out_path):
    os.makedirs(out_path)

frequency = np.genfromtxt(in_file,  dtype = np.str, delimiter = ",")

# Remove HashMap brackets
frequency[0] = frequency[0][1:]
frequency[-1] = frequency[-1][:-1]

for i in range(frequency.shape[0]):
    frequency[i] = frequency[i].split('=')[-1]

frequency = frequency.astype("float64")

# Remove elements that are 0
frequency = np.delete(frequency, np.where(frequency == 0))

# Save formated data to CSV
np.savetxt(csv_file, frequency)

rcParams["axes.titlepad"] = 20
rcParams["axes.labelpad"] = 20
plt.style.use("ggplot")

fig, ax = plt.subplots()

ax.set_title(r"$\bf{" + args.dataset + "}$" + "\nFrequency of trees in GSGP individuals".format(args.dataset))
#ax.set_title("Frequency of trees in GSGP individuals")
ax.set_xlabel("Individual")
ax.set_ylabel("Frequency")
ax.set_yscale("log")

ax.bar(np.arange(len(frequency)) + 1, frequency)

plt.savefig("{}/frequency.png".format(out_path), dpi=300, bbox_inches="tight")

"""
plt.show(block=False)
input("Hit Enter To Close")
plt.close()
"""
