#!/bin/sh

w=(5 10 15 20 30 50)
rm './outFiles/dw.txt'
touch './outFiles/dw.txt'

for w in "${w[@]}"
do
    echo "Running with w = $w"
    echo "$w" >> './outFiles/dw.txt'
    ./run.sh true 3 $w
    cd graphics
    python3 dw.py
    cd ..
done

cd graphics
python3 flow.py
