import numpy as np
from matplotlib import pyplot as plt

dt = 50

with open("../outFiles/dw.txt", "r") as dw_file:
    line = dw_file.readline()
    while line:
        prev_y = 0
        prev_x = -1
        flow = []

        w = line[:-1]
        line = dw_file.readline()
        parts = line.split(" ")
        xs = []
        ys = []
        while len(parts) == 2:
            x = float(parts[0]) # t
            y = int(parts[1]) # n

            # if x != prev_x:
            #     for i in range(prev_x+1, x+1):
            xs.append(x)
            ys.append(y)
                
            prev_x = x

            # if x >= dn:
            #     print("{} {}".format(x, y))
            #     xs.append(x)
            #     flow.append((x - prev_x) / (y - prev_y))
            #     prev_x = x
            #     prev_y = y
            #     target += dn

            line = dw_file.readline()
            parts = line.split(" ")
        
        
        flow_x = []
        for i in range(dt, len(ys)):
            val = ys[i] - ys[i - dt]
            flow_x.append(xs[i])
            flow.append(val / (xs[i] - xs[i - dt]))

        # flow_x = np.arange(0, len(flow), 1)
        plt.xlabel("t")
        plt.ylabel("Q")
        plt.scatter(flow_x, flow, label=w)
        plt.show()

            
dw_file.close()

