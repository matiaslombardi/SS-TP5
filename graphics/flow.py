dt_out = {"0": [0, 0, 0, 0]}
ws = []

with open("../outFiles/dw.txt", "r") as dw_file:
    line = dw_file.readline()
    while line:
        ws.append(line[:-1])
        line = dw_file.readline()
        parts = line.split(" ")
        while len(parts) == 2:
            if parts[0] not in dt_out:
                dt_out[parts[0]] = []
            
            dt_out[parts[0]].append(parts[1][:-1])
            line = dw_file.readline()
            parts = line.split(" ")
dw_file.close()

dt_out["0"] = [0 for _ in ws]

# with open("../outFiles/flow_out.txt", "w") as flow_file:
#     print(ws)
#     flow_file.write("{},{}\n".format("step", ','.join(ws)))
#     for key in dt_out:
#         flow_file.write("{},{}\n".format(key, ','.join(dt_out[key])))
# flow_file.close()
import numpy as np

import matplotlib
matplotlib.use('agg')

from matplotlib import pyplot as plt

dts = np.array([k for k in dt_out.keys()])

plt.figure(figsize=(15, 8))

for i in range(len(ws)):
    plt.plot(dts, [dt_out[t][i] for t in dts], label=ws[i])

plt.xticks(dts[np.arange(0, len(dts), 10000)])
plt.legend()
plt.savefig("../outFiles/flow.png")
