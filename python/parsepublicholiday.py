import csv
import time
import re
import pymysql
from datetime import datetime
from datetime import timedelta
nat=re.compile(r'NAT')
vic=re.compile(r'VIC')
holidays=set()
#paths=['australianpublicholidays-201415.csv','australianpublicholidays-201516.csv','australianpublicholidays-201617.csv','australianpublicholidays-201718.csv']
paths=['australianpublicholidays-201617.csv','australianpublicholidays-201718.csv']
def add(path):
    with open(path,'r',newline='') as f:
        reader= csv.DictReader(f)
        for row in reader:
            if nat.search(row.get('Applicable To')) or vic.search(row.get('Applicable To')):
                atime = datetime.strptime(row.get('Date'),'%Y%m%d')
                date=atime.date()
                holidays.add(date)

for path in paths:
    add(path)



db = pymysql.connect(host="115.146.84.182", port=3306, password='**********', user='root', db='project',
                     cursorclass=pymysql.cursors.DictCursor)
print('successful connection')
cursor = db.cursor()
stations=[10001,10003,10017,10107,10217,10218,10219,10239]
# for station in stations:
#     get_data = "select * from LSTM.`{}`".format(str(station))
#     cursor.execute(get_data)
#     results = cursor.fetchall()
#     back_value={}
#     for row in results:
#         date=row.get('time')
#         for holiday in holidays:
#             diff=date-holiday
#             if diff.days==0:
#                 back_value[row.get('ID')]=1
#                 print('ID:{} in station:{} is holiday: {} '.format(row.get('ID'),station,holiday))
#         if row.get('ID') not in back_value:
#             back_value[row.get('ID')] = 0
#     for (id,value) in back_value.items():
#         update_sql='update LSTM.`{}` set LSTM.`{}`.holiday={} where LSTM.`{}`.ID={}'.format(station,station,value,station,id)
#         ste=cursor.execute(update_sql)
#         db.commit()
get_data = "select * from project.air where stationID={}".format(str(10001))
cursor.execute(get_data)
results = cursor.fetchall()
for row in results:
    back_value={}
    date=row.get('time')
    # delta = timedelta(hours=1)
    # timeone=datetime(results[0]['time'].year,results[0]['time'].month,results[0]['time'].day)+delta
    # print(timeone)
    # second_delta=timedelta(hours=2)
    # timetwo=datetime(results[0]['time'].year,results[0]['time'].month,results[0]['time'].day)+second_delta
    # print(timetwo)
    # diff=timetwo-timeone
    # print(diff.seconds)
    onlyday=date.date()
    for holiday in holidays:
            diff=onlyday-holiday
            if diff.days==0:
                back_value[row.get('ID')]=1
    if row.get('ID') not in back_value:
             back_value[row.get('ID')] = 0
    for (id, value) in back_value.items():
        update_sql='update project.air set project.air.holiday={} where project.air.ID={}'.format(value,id)
        ste=cursor.execute(update_sql)
        db.commit()