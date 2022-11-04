import numpy as np

import matplotlib
matplotlib.use('agg')

from matplotlib import pyplot as plt


def linear_regression(x, y):
    size = np.size(x)
  
    mean_x = np.mean(x)
    mean_y = np.mean(y)
  
    SS_xy = np.sum(y * x) - size * mean_x * mean_y
    SS_xx = np.sum(x * x) - size * mean_x * mean_x
  
    b1 = SS_xy / SS_xx
    b0 = mean_y - b1 * mean_x

    SS_E = np.sum((y - mean_y) ** 2)
    s_2_yx = SS_E / (size - 2)

    s_2_m = s_2_yx / SS_xx
  
    return (b0, b1, s_2_m ** 0.5)

def plot_line(x, y, label):
    b0, b1, err = linear_regression(x, y)
    y_pred = b0 + b1 * x
    # err = mean_squared_error(y, y_pred)

    print("w = {}, b1 = {}, err = {}".format(label, b1, err))

    # plt.errorbar(x, y_pred, yerr=err, errorevery=20000, capsize=2.0, label=label)
    plt.plot(x, y_pred, label=label)
    return (b0, b1, err)

# def mean_squared_error(y, y_pred):
#     return np.mean((y - y_pred) ** 2)
    
plt.figure(figsize=(16, 10))

max_dt = 0
max_y = 0

bs = []
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

            if (x > 10):
                xs.append(x)
                ys.append(y)
            line = dw_file.readline()
            parts = line.split(" ")
        
        if xs[-1] > max_dt:
            max_dt = xs[-1]
        
        if ys[-1] > max_y:
            max_y = ys[-1]

        b0, b1, err = plot_line(np.array(xs), np.array(ys), w)
        bs.append([w, b0, b1, err])
        dts = xs
        
dw_file.close()

plt.xlabel("Tiempo (s)", size=14)
plt.ylabel("Cantidad", size=14)

plt.xticks(np.arange(0, max_dt+1, 50))
plt.yticks(np.arange(0, max_y+1, 5))
plt.tick_params(labelsize=14)

plt.legend()
plt.savefig("../outFiles/flow_linear.png")

with open("../outFiles/b.txt", "w") as b_file:
    for [w, b0, b1, err] in bs:
        b_file.write("{} {} {} {}\n".format(w, b0, b1, err))
