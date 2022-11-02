#!/bin/sh

D=(3 4 5 6)
rm './outFiles/dd.txt'
touch './outFiles/dd.txt'

for d in "${D[@]}"
do
    echo "Running with d = $d"
    echo "$d" >> './outFiles/dd.txt'
    ./run.sh true $d 20
    cd graphics
    python3 dd.py
    cd ..
done

cd graphics
python3 flow_dd.py
