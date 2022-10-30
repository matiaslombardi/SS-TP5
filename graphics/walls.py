width = 20
height = 70
slit = 4
id = 501
with open('../outFiles/yPos.txt', 'r') as y_pos_file:
    with open('../outFiles/walls.txt', 'w') as wall_file:    
        for line in y_pos_file:
            y_pos = float(line)
            wall_file.write(str(2*(width+height)+1-slit) +"\n")
            wall_file.write("position\n")
            # Horizontal walls
            for x in range(0, width):
                if (x <= (width/2 - slit/2)) or (x >= (width/2 + slit/2)):
                    wall_file.write(str(id) + " " +str(x) + " " + str(y_pos) + " 0.3\n")
                    id+=1
                wall_file.write(str(id) + " " +str(x) + " " + str(height+y_pos) + " 0.3\n")
                id+=1
            
            # Vertical walls
            for y in range(0, height):
                wall_file.write(str(id) + " " +str(0) + " " + str(y+y_pos) + " 0.3\n")
                id+=1
                wall_file.write(str(id) + " " +str(width) + " " + str(y+y_pos) + " 0.3\n")
                id+=1

            id = 501
    
        wall_file.close()
    y_pos_file.close()
