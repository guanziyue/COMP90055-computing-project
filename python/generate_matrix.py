import pymysql
import copy
from datetime import timedelta
from collections import deque
from datetime import datetime
import numpy as np
import csv

# first initialize feature list
days_in_week = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
weeks_in_year = []
hours_in_day = []
features = []
for i in range(24):
    hours_in_day.append('hour{}'.format(i))
for i in range(0, 54):
    weeks_in_year.append('week{}'.format(i))
features.extend(weeks_in_year)
features.extend(days_in_week)
features.extend(hours_in_day)
features.append('holiday')
features.append('traffic')
features.append('value')
# write a readme file to clarify the structure
f = open('readme.txt', 'w')
for i in range(len(features)):
    f.write(str(features[i]) + ',')
f.write('\r\n')
f.write(
    'This file is used to show the structure of seq_dataset.npy.This is a three dimension tensor. shape is (num_of_samples,seven_piece_in_one_sequence,num_of_features)')
f.write('\r\n')
f.write('feature sequence is showed above')
f.write('\r\n')
print('features written')
# connect database
db = pymysql.connect(host="115.146.84.182", port=3306, password='**********', user='root', db='project',
                     cursorclass=pymysql.cursors.DictCursor)
print('successful connection')
cursor = db.cursor()
# stations = [10001, 10003, 10017, 10107, 10217, 10218, 10219, 10239]
stations = [10001]


# convert a sequence to a numpy matrix. shape is (7,features).
def convert_to_np_matrix(queue, features):
    this_sample = np.zeros(shape=(7, len(features)))
    for i, piece in enumerate(queue):
        for k, feature in enumerate(features):
            this_sample[i][k] = queue[i][feature]
    return this_sample


def output_to_csv(alist, station, num):
    new_header = copy.deepcopy(features)
    new_header.insert(0, 'time')
    filename = 'station_{}_{}.csv'.format(station, num)
    with open(filename, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=new_header)
        writer.writeheader()
        for piece in alist:
            print(piece)
            writer.writerow(piece)


#
# data = []
# this_station_data = []
# sample_queue = deque(maxlen=7)
for station in stations:
    print('start read {}'.format(station))
    get_data = "select * from project.air where nearwind is not null and stationID=10001 order by time"
    cursor.execute(get_data)
    results = cursor.fetchall()
    count = 0
    data = []
    this_station_data = []
    sample_queue = deque(maxlen=7)
    for row in results:
        # create a dictionary to store some features.
        row_feature = {}
        for weekday in days_in_week:
            row_feature[weekday] = 0
        for hour in hours_in_day:
            row_feature[hour] = 0
        for week in weeks_in_year:
            row_feature[week] = 0
        row_feature['holiday'] = 0
        weekend = int(row.get('weeekend'))
        row_feature[days_in_week[weekend - 1]] = 1
        row_feature['holiday'] = row.get('holiday')
        week_of_year = int(row.get('week'))
        row_feature['week{}'.format(week_of_year)] = 1
        date = row.get('time')
        row_feature['time'] = date
        row_feature['traffic'] = row.get('trafficvalue')
        print('current station:{} time:{}'.format(station, date))
        row_feature['hour{}'.format(date.hour)] = 1
        row_feature['value']=row.get('value')
        if len(sample_queue) > 0:
            diff = row_feature['time'] - sample_queue[-1]['time']
            if diff.seconds == 3600:
                sample_queue.append(row_feature)
                if len(sample_queue) == 7:
                    data.append(copy.deepcopy(sample_queue))
                    this_station_data.append(copy.deepcopy(sample_queue))
            elif diff.seconds > 3600:
                sample_queue = deque(maxlen=7)
                sample_queue.append(row_feature)
        else:
            sample_queue.append(row_feature)
        # for i in range(24):
        #     this_features = copy.deepcopy(row_feature)
        #     this_features['hour{}'.format(i)] = 1
        #     value = row.get(str(i))
        #     try:
        #         check=float(value)
        #     except:
        #         print(value)
        #         value = 0
        #     this_features['value']=value
        #     delta = timedelta(hours=i)
        #     this_features['time'] = datetime(this_features['time'].year, this_features['time'].month,
        #                                      this_features['time'].day) + delta
        #     if len(sample_queue) > 0:
        #         diff = this_features['time'] - sample_queue[-1]['time']
        #         if diff.seconds == 3600:
        #             sample_queue.append(this_features)
        #             if len(sample_queue)==7:
        #                 data.append(copy.deepcopy(sample_queue))
        #                 this_station_data.append(copy.deepcopy(sample_queue))
        #         elif diff.seconds > 3600:
        #             sample_queue=deque(maxlen=7)
        #             sample_queue.append(this_features)
        #             # output_to_csv(sample_queue, station, count)
        #             # count += 1
        #             # sample_queue = []
        #             # sample_queue.append(this_features)
        #     else:
        #         sample_queue.append(this_features)
    this_result = np.zeros((len(this_station_data), 7, len(features)))
    f.write('station:{} has shape is ({},{},{})'.format(station, len(this_station_data), 7, len(features)))
    for i, pickdata in enumerate(this_station_data):
        for k in range(7):
            print(pickdata[k])
            for j in range(len(features)):
                this_result[i][k][j] = pickdata[k][features[j]]
    filename = 'station_10001_traffic.npy'
    np.save(filename, this_result)
    # output_to_csv(sample_queue, station, count)
    count += 1

# #save final result
# final_result = np.zeros((len(data), 7, len(features)))
# f.write('final shape is ({},{},{})'.format(len(data), 7, len(features)))
# for i, pickdata in enumerate(data):
#     for k in range(7):
#         for j in range(len(features)):
#             final_result[i][k][j] = pickdata[k][features[j]]
# np.save('seq_dataset.npy', final_result)
# f.close()
