import pymysql
import numpy as np
import csv

db = pymysql.connect(host="115.146.84.182", port=3306, password='allenguan01', user='root', db='project',
                     cursorclass=pymysql.cursors.DictCursor)
print('successful connection')
cursor = db.cursor()
get_data = "select *,hour(time) as hour from air where nearwind is not null"
data = []
days_in_week = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
weeks_in_year = []
hours_in_day = []
headers = ['time']
for i in range(1, 53):
    weeks_in_year.append('week{}'.format(i))
headers.extend(weeks_in_year)
headers.extend(days_in_week)
for i in range(24):
    hours_in_day.append('hour{}'.format(i))
headers.extend(hours_in_day)
headers.append('trafficvalue')
headers.append('wind')
headers.append('rain')
headers.append('holiday')
headers.append('value')

cursor.execute(get_data)
results = cursor.fetchall()
print('SQL query successfully')
for row in results:
    piece = {}
    for weekday in days_in_week:
        piece[weekday] = 0
    for hour in hours_in_day:
        piece[hour] = 0
    for week in weeks_in_year:
        piece[week] = 0
    piece['time'] = row.get('time')
    piece['trafficvalue'] = row.get('trafficvalue')
    piece['wind'] = row.get('nearwind')
    piece['rain'] = row.get('nearrain')
    weekend = int(row.get('weeekend'))
    piece[days_in_week[weekend - 1]] = 1
    piece['holiday'] = row.get('holiday')
    piece['value'] = row.get('value')
    week_of_year = int(row.get('week'))
    hour = int(row.get('hour'))
    piece['week{}'.format(week_of_year)] = 1
    piece['hour{}'.format(hour)] = 1
    print(piece)
    data.append(piece)
np.random.shuffle(data)
train_set = data[:-3000]
test_set = data[-3000:]
print ('train set length{}, test set length {}'.format(len(train_set),len(test_set)))
print(headers)
with open('train_set.csv', 'w+',newline='') as train_set_writer:
    writer = csv.DictWriter(train_set_writer, fieldnames=headers)
    writer.writeheader()
    for piece in train_set:
        writer.writerow(piece)
with open('test_set.csv', 'w+',newline='') as test_set_writer:
    writer = csv.DictWriter(test_set_writer, fieldnames=headers)
    writer.writeheader()
    for piece in test_set:
        writer.writerow(piece)
