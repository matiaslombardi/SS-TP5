width = 20
height = 70
slit = 10
id = 500
with open('../outFiles/yPos.txt', 'r') as y_pos_file:
    with open('../outFiles/walls.txt', 'w') as wall_file:    
        for line in y_pos_file:
            y_pos = float(line)
            wall_file.write(str(2*(width+height)) +"\n")
            wall_file.write("position\n")
                # Horizontal walls
            for x in range(0, width):
                wall_file.write(str(id) + " " +str(x) + " " + str(y_pos) + "\n")
                id+=1
                wall_file.write(str(id) + " " +str(x) + " " + str(height+y_pos) + "\n")
                id+=1
            
            # Vertical walls
            for y in range(0, height):
                wall_file.write(str(id) + " " +str(0) + " " + str(y+y_pos) + "\n")
                id+=1
                wall_file.write(str(id) + " " +str(width) + " " + str(y+y_pos) + "\n")
                id+=1

                # # Only write wall if it is not in the hole, hole is centered vertically
                # if y < ((height/2) - slit/2) or y > ((height/2) + slit/2):
                #     s+=(str(id) + " " + str(width/2) + " " + str(y) + "\n")
                #     id+=1
        wall_file.close()
    y_pos_file.close()
