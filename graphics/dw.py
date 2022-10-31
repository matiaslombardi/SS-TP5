tot = 0
with open('../outFiles/flow.txt', 'r') as flow_file:
    with open('../outFiles/dw.txt', 'a') as dw_file:
        line = flow_file.readline()
        while line:
            [step, flow] = line.split(' ')
            tot += int(flow)
            dw_file.write("{} {}\n".format(step, tot))
            line = flow_file.readline()


