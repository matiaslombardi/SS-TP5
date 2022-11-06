import numpy as np

import matplotlib
matplotlib.use('agg')

from matplotlib import pyplot as plt

plt.figure(figsize=(16, 10))
max_dt = 0
max_y = 0
with open("../outFiles/dw.txt", "r") as dw_file:
    line = dw_file.readline()
    while line:
        w = line[:-1]
        line = dw_file.readline()
        parts = line.split(" ")
        xs = [0]
        ys = [0]
        while len(parts) == 2:
            xs.append(float(parts[0]))
            ys.append(int(parts[1]))
            line = dw_file.readline()
            parts = line.split(" ")
        
        if xs[-1] > max_dt:
            max_dt = xs[-1]
        
        if ys[-1] > max_y:
            max_y = ys[-1]

        plt.plot(xs,  ys, label=w)

        dts = xs
        
dw_file.close()

plt.xlabel("Tiempo (s)", size=14)
plt.ylabel("Cantidad", size=14)

plt.xticks(np.arange(0, max_dt+1, 50))
plt.yticks(np.arange(0, max_y+1, 5))
plt.tick_params(labelsize=14)

plt.legend()
plt.savefig("../outFiles/flow.png")
