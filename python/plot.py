import pymysql
import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import csv

db = pymysql.connect(host="115.146.84.182", port=3306, password='allenguan01', user='root', db='project',
                     cursorclass=pymysql.cursors.DictCursor)
print('successful connection')
cursor = db.cursor()
get_data = "select value,weeekend from air"
cursor.execute(get_data)
results = cursor.fetchall()
print('SQL query successfully')
x=[]
y=[]
for row in results:
    if row.get('value')<=100 and row.get('weeekend')>=0:
        x.append(row.get('weeekend')+1)
        y.append(row.get('value'))
plt.ylabel("PM2.5 Value")
plt.xlabel("weekday")
plt.title('PM2.5 value vs weekday')
plt.scatter(x,y,marker='.')
plt.show()
# for row in results:
#         x.append(row.get('weeekend')+1)
#         y.append(row.get('value'))
# plt.ylabel("PM2.5 Value")
# plt.xlabel("weekday")
# plt.title('PM2.5 Value vs weekday')
# plt.scatter(x,y,marker='.')
# plt.show()