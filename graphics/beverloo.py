import matplotlib

matplotlib.use('agg')

from matplotlib import pyplot as plt

import math
import numpy as np

xs = []
ys = []

density = 200 / (40 * 70)
sqrt_g = 5 ** 0.5
k = 1

def beverloo(c, d):
    return c * sqrt_g * density * ((d - k) ** 1.5) 

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
initial_c = 0.6
min_err = math.inf
min_c = 0


step = 0.001

print(initial_c)
cs = np.arange(initial_c - iters * step, initial_c + iters * step, step)
for c in cs:
    error = 0

    for j in range(len(ys)):
        q = beverloo(c, xs[j])
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

bev_xs = np.arange(xs[0], xs[-1] + 0.1, 0.1)
bev_ys = [beverloo(min_c, d) for d in bev_xs]
plt.plot(bev_xs, bev_ys)

plt.savefig("../outFiles/beverloo.png")




