import numpy as np

import matplotlib
matplotlib.use('agg')

from matplotlib import pyplot as plt

dn = 10

with open("../outFiles/dw.txt", "r") as dw_file:
    line = dw_file.readline()
    while line:
        target = dn
        prev_y = 0
        prev_x = -1
        flow = []

        w = line[:-1]
        line = dw_file.readline()
        parts = line.split(" ")
        xs = []
        ys = []
        while len(parts) == 2:
            y = float(parts[0])  # t
            x = int(parts[1])  # n

            if x != prev_x:
                for i in range(prev_x + 1, x + 1): # hasta x inclusive
                    xs.append(i)
                    ys.append(y)

                prev_x = x
            
            line = dw_file.readline()
            parts = line.split(" ")

        flow_x = []
        for i in range(dn, len(ys)):
            val = ys[i] - ys[i - dn]
            flow_x.append(ys[i])
            flow.append((xs[i] - xs[i - dn]) / val)

        plt.xlabel("Tiempo (s)") # TODO: es realmente tiempo o un delta tiempo?
        plt.ylabel("Q")
        plt.plot(flow_x, flow, '-o')

        plt.savefig("../outFiles/flow_" + w + ".png")
        plt.clf()

        print("w = {}".format(w))
        print(np.mean(flow[15:]))
        print(np.std(flow[15:], ddof=1))

dw_file.close()
