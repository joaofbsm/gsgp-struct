#!/usr/bin/env python3

"""Plot the trees that represents GSGP individuals"""

__author__ = "João Francisco Barreto da Silva Martins"
__email__ = "joaofbsm@dcc.ufmg.br"
__license__ = "GPL"
__version__ = "3.0"

import os
import argparse
import networkx as nx
from shell_command import shell_call
from networkx.drawing.nx_agraph import write_dot


def read_repr(file_path):
    """Read individual representation from given file.
    
    Arguments:
        file_path -- Path to file that stores the individual representation.
    """

    with open(file_path, 'r') as f:
        freq = f.readline()
        repr_ = f.readline()
    return freq, repr_


def parse_repr(repr_):
    """Parse the string representation of the individual.
    
    Arguments:
        repr_ -- String representation of the individual.
    """

    functions = set(['+', '-', '*', "AQ"])

    labels = {}
    edges = []

    repr_ = repr_.replace('(', '')
    repr_ = repr_.split()

    n = 0
    root = repr_[0]

    if root in functions:
        labels[n] = (root, "#ff0000")
    else:
        labels[n] = (root, "#00ff00")
    parent_stack = [n]

    n += 1
    for node in repr_[1:]:
        if node == ')':
            parent_stack.pop()
            continue

        edges.append((parent_stack[-1], n))

        if node in functions:
            labels[n] = (node, "#ff0000")
            parent_stack.append(n)
        else:
            labels[n] = (node, "#00ff00")

        n += 1

    return labels, edges
    

def visualize_individual(repr_, freq, dir_path, name):
    """Visualize graphically the tree that represents the individual.
    
    Arguments:
        repr_ -- Representation of the individual to be visualized.
    """

    dot_dir_path = "{}/dot".format(dir_path)
    png_dir_path = "{}/png".format(dir_path)

    if not os.path.exists(dot_dir_path):
        os.makedirs(dot_dir_path)
    if not os.path.exists(png_dir_path):
        os.makedirs(png_dir_path)

    dot_file_path = "{}/{}.dot".format(dot_dir_path, name)
    png_file_path = "{}/{}.png".format(png_dir_path, name)

    labels, edges = parse_repr(repr_)

    g = nx.DiGraph()

    # Use HTML like syntax so title can have padding from graph
    g.graph['graph']={"label" : "<Frequency = {} <br/> <br/> <br/>>".format(freq), "labelloc" : "t"}

    for key, value in labels.items():
        g.add_node(key, style="filled", label=value[0], fillcolor=value[1])
    g.add_edges_from(edges)

    write_dot(g, dot_file_path)
    shell_call("dot -Tpng -Gdpi=300 {} > {}".format(dot_file_path, png_file_path))


def main(args):
    results_dir = "/Users/joaofbsm/Documents/UFMG/2017-2/POC1/implementation/results/" + args.algorithm
    dir_path = results_dir + "/output-" + args.dataset +  "/individuals"

    if args.file is None:
        num_trees = 0
        for file in os.listdir(dir_path):
            if file.endswith(".txt"):
                num_trees += 1

        for i in range(num_trees):
            freq, repr_ = read_repr("{}/{}.txt".format(dir_path, i))
            visualize_individual(repr_, freq, dir_path, i)
    else:
        freq, repr_ = read_repr("{}/{}.txt".format(dir_path, args.file))
        visualize_individual(repr_, freq, dir_path, args.file)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Plotting (GS)GP Individuals")
    parser.add_argument("-d", "--dataset")
    parser.add_argument("-a", "--algorithm", default="gsgp")
    parser.add_argument("-f", "--file", nargs="?")  # Argument is optional
    args = parser.parse_args()

    main(args)
