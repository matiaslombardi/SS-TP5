import matplotlib
# matplotlib.use('agg')

from matplotlib import pyplot as plt

xs = []
ys = []
errors = []

with open("../outFiles/b.txt", "r") as b_file:
    line = b_file.readline()
    while line:
        [x, _, b1, err] = line.split(' ')
        xs.append(float(x))
        ys.append(float(b1))
        errors.append(float(err))
        line = b_file.readline()

plt.errorbar(xs, ys, yerr=errors, fmt='o', ecolor='r', capthick=2, capsize=5)
plt.show()