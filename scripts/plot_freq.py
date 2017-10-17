#!/usr/bin/env python3

"""Plot the frequency of appearence of initial population generated trees"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "GPL"
__version__ = "3.0"

import argparse
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import rcParams

parser = argparse.ArgumentParser(description="Plotting Tree Frequency in GSGP")
parser.add_argument("-d", "--dataset")
args = parser.parse_args()

file_name = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results/{}.txt".format(args.dataset)
frequency = np.genfromtxt(file_name,  dtype = np.str, delimiter = ",")

# Remove HashMap brackets
frequency[0] = frequency[0][1:]
frequency[-1] = frequency[-1][:-1]

for i in range(frequency.shape[0]):
    frequency[i] = frequency[i].split('=')[-1]

frequency = frequency.astype("float64")

# Remove elements that are 0
frequency = np.delete(frequency, np.where(frequency == 0))

rcParams["axes.titlepad"] = 20
rcParams["axes.labelpad"] = 20
plt.style.use("ggplot")

fig, ax = plt.subplots()

ax.set_title("Frequency of trees in GSGP individuals(Pop. size = 1000)")
ax.set_xlabel("Individual")
ax.set_ylabel("Frequency")
ax.set_yscale("log")

ax.bar(np.arange(len(frequency)) + 1, frequency)

plt.show(block=False)
input("Hit Enter To Close")
plt.close()
