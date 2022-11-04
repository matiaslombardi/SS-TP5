import scipy as sp
import numpy as np
import matplotlib
from scipy import stats
matplotlib.use('agg')

from matplotlib import pyplot as plt

bs = []
max_dt = 0
max_y = 0

with open("../outFiles/dw.txt", "r") as dw_file:
    line = dw_file.readline()
    while line:
        w = line[:-1]
        line = dw_file.readline()
        parts = line.split(" ")
        xs = []
        ys = []
        while len(parts) == 2:
            x = float(parts[0])
            y = float(parts[1])

            if (x > 200):
                xs.append(x)
                ys.append(y)
            line = dw_file.readline()
            parts = line.split(" ")
        
        if xs[-1] > max_dt:
            max_dt = xs[-1]
        
        if ys[-1] > max_y:
            max_y = ys[-1]
        
        slope, intercept, rv, pv, std_slope = stats.linregress(xs, ys, alternative='two-sided')
        bs.append([w, slope, intercept, rv, pv, std_slope])
        print("w: {}, slope: {}, err: {}".format(w, slope, std_slope))
        xs = np.array(xs)
        y_pred = intercept + slope * xs
        plt.plot(xs, y_pred, label=w)
        dts = xs
        
dw_file.close()

plt.legend()
plt.savefig("../outFiles/flow_linear.png")