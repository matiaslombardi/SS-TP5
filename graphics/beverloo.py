import matplotlib
import math
import numpy as np
matplotlib.use('agg')

from matplotlib import pyplot as plt

xs = []
ys = []

with open("../outFiles/b_dd.txt", "r") as flow_file:
    line = flow_file.readline()
    while line:
        [w, _, b1, __] = line.split(" ")
        w = int(w)
        xs.append(w)
        b1 = float(b1)
        ys.append(b1)
        line = flow_file.readline()

iters = 500
errors = []
initial_c = 3.464
min_err = math.inf
min_c = 0

density = 200 / (40 * 70)
sqrt_g = 5 ** 0.5
step = 0.001

print(initial_c)
cs = np.arange(initial_c - iters * step, initial_c + iters * step, step)
for c in cs:
    error = 0

    for j in range(len(ys)):
        q = density * sqrt_g * (abs(xs[j] - c) ** 1.5)
        error += (ys[j] - q) ** 2
    
    if error < min_err:
        min_err = error
        min_c = c

    errors.append(error)

plt.plot(cs, errors)

print(min_c, min_err)
plt.xlabel("c")
plt.ylabel("Error")

plt.savefig("../outFiles/beverloo_err.png")

plt.clf()

plt.scatter(xs, ys)

bev_xs = np.arange(xs[0], xs[-1], 0.1)
bev_ys = [(density * sqrt_g * (abs(d - min_c) ** 1.5)) for d in bev_xs]
plt.plot(bev_xs, bev_ys)

plt.savefig("../outFiles/beverloo.png")




