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
        [d, _, b1] = line.split(" ")
        d = int(d)
        xs.append(d)
        b1 = float(b1)
        ys.append(b1)
        line = flow_file.readline()

iters = 500
errors = []
initial_c = 2.5
min_err = math.inf
min_c = 0

# TODO
#la densidad del material cuando esta aplastada. 
# Ver la altura de la particula mas alta de cuando quedan apiladas abajo alrededor de 35

# TODO
#Ver con aperturas de 5 a 10 (con vibrar) 

density = 200 / (20 * 32) 
sqrt_g = 5 ** 0.5
step = 0.001

cs = np.arange(initial_c - iters * step, initial_c + iters * step, step)
for c in cs:
    error = 0

    for j in range(len(ys)):
        q = density * sqrt_g * ((xs[j] - c) ** 1.5)
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
bev_ys = [(density * sqrt_g * ((d - min_c) ** 1.5)) for d in bev_xs]
plt.plot(bev_xs, bev_ys)

plt.savefig("../outFiles/beverloo.png")




