import pymysql
import numpy as np
import csv

db = pymysql.connect(host="115.146.84.182", port=3306, password='allenguan01', user='root', db='project',
                     cursorclass=pymysql.cursors.DictCursor)
print('successful connection')
cursor = db.cursor()
total=[]
header=['stationID','time']
for i in range(24):
    header.append(str(i))
stations=[10001,10003,10017,10107,10217,10218,10219,10239]
for station in stations:
    get_data = "select * from LSTM.`{}`".format(str(station))
    data = []
    cursor.execute(get_data)
    results = cursor.fetchall()
    print('SQL query successfully: {}'.format(station))
    for row in results:
        piece={}
        for column in header:
            piece[column]=row.get(column)
        data.append(piece)
    np.random.shuffle(data)
    with open('{}.csv'.format(station), 'w+',newline='') as train_set_writer:
        writer = csv.DictWriter(train_set_writer, fieldnames=header)
        writer.writeheader()
        for piece in data:
            writer.writerow(piece)
    total.extend(data)
with open('whole.csv','w',newline='') as f:
    writer=csv.DictWriter(f, fieldnames=header)
    writer.writeheader()
    for piece in total:
        writer.writerow(piece)

