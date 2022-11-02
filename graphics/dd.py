tot = 0
with open('../outFiles/flow.txt', 'r') as flow_file:
    with open('../outFiles/dd.txt', 'a') as dd_file:
        line = flow_file.readline()
        while line:
            [step, flow] = line.split(' ')
            tot += int(flow)
            dd_file.write("{} {}\n".format(step, tot))
            line = flow_file.readline()
