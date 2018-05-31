import tensorflow as tf
import keras
from keras.models import Sequential
from keras.layers.core import Dense, Activation, Lambda
from keras.layers import LeakyReLU
from keras.layers.recurrent import SimpleRNN, LSTM, GRU
from keras.initializers import RandomUniform
from keras.callbacks import TensorBoard
from keras.optimizers import SGD
from keras.callbacks import CSVLogger
import numpy as np
from pandas import concat
from pandas import DataFrame
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import time
from keras.models import load_model


# this is a function modified from https://machinelearningmastery.com/convert-time-series-supervised-learning-problem-python/?spm=a2c4e.11153940.blogcont174270.13.3f8b65ffx3OaFF
# to convert a series data to supervised machine learning problem

def generate_headers():
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
    features.append('value')
    return features


def series_to_supervised(data, n_in=1, n_out=1, dropnan=True):
    """
    Frame a time series as a supervised learning dataset.
    Arguments:
        data: Sequence of observations as a list or NumPy array.
        n_in: Number of lag observations as input (X).
        n_out: Number of observations as output (y).
        dropnan: Boolean whether or not to drop rows with NaN values.
    Returns:
        Pandas DataFrame of series framed for supervised learning.
    """
    headers = generate_headers()
    feature_amount = len(headers)
    df = DataFrame(data)
    columns, new_features = list(), list()
    # input sequence (t-n, ... t-1)
    for i in range(n_in, 0, -1):
        columns.append(df.shift(i))
        new_features += [('var%d(t-%d)' % (j + 1, i)) for j in range(feature_amount)]
    # forecast sequence (t, t+1, ... t+n)
    for i in range(0, n_out):
        columns.append(df.shift(-i))
        if i == 0:
            new_features += [('var%d(t)' % (j + 1)) for j in range(feature_amount)]
        else:
            new_features += [('var%d(t+%d)' % (j + 1, i)) for j in range(feature_amount)]
    # put it all together
    agg = concat(columns, axis=1)
    agg.columns = new_features
    # drop rows with NaN values
    if dropnan:
        agg.dropna(inplace=True)
    return agg


def get_dataset():
    dataset = np.load('station_10001_traffic.npy')
    X = dataset[:, 0:6, :]
    X[:, 0:6, -1] = X[:, 0:6, -1]/80
    X[:, 0:6, -2] = X[:, 0:6, -2] /2000
    y = dataset[:, 6, -1]
    y = y/80
    X_train, X_other, y_train, y_other = train_test_split(X, y, test_size=0.2, shuffle=True)
    X_dev, X_test, y_dev, y_test = train_test_split(X_other, y_other, test_size=0.5, shuffle=True)
    return X_train, X_dev, X_test, y_train, y_dev, y_test


# get dataset from file. shape of X is (sample amount,6,87), shape of y is (sample amount,1,87).
# 87 is feature numbers. 6 is length of sequence. y is output, which is the next time value after six sequence.
X_train, X_dev, X_test, y_train, y_dev, y_test = get_dataset()
count_test=[]
for i in range(y_test.shape[0]):
    if y_test[i] >=0.5:
        count_test.append(i)
special_test = np.zeros((len(count_test),X_test.shape[1],X_test.shape[2]))
special_result =  np.zeros((len(count_test)))
print(len(count_test))
for i,k in enumerate(count_test):
    special_test[i]=X_test[k]
    special_result[i]=y_test[k]
# set hypermeters
optimizer = 'sgd'
learning_rate = 0.01
decay = 1e-6
momentum = 0.8
activation_method = 'linear'
activation_function = LeakyReLU(alpha=0.3)
np.mean(X_train[:][:][-1])
layer_weight_initializer_min = 0.0005
layer_weight_initializer_max = 0.0015
random_initializer = RandomUniform(minval=layer_weight_initializer_min, maxval=layer_weight_initializer_max)
sgd = SGD(lr=learning_rate, decay=decay, momentum=momentum, nesterov=True)
layer_amount = 1
batch_size = 3200
iteration_times = 20000

# recorder of training process
csv_logger = CSVLogger('log.csv', append=True, separator=';')
localtime = time.strftime('%Y-%m-%d_%H%M%S', time.localtime(time.time()))
txt_name = str(localtime + '.txt')
# set LSTM layer
LSTM_layer = LSTM(87, input_shape=(6, X_train.shape[2]),
                  kernel_initializer=random_initializer,implementation=2)
# set up model
# model = Sequential()
# model.add(LSTM_layer)
# model.add(Dense(1, use_bias=False, kernel_initializer=random_initializer))
# model.compile(optimizer=sgd, loss='mean_squared_error', metrics=['accuracy'])
model=load_model('2018-05-07_214720.h5')
history = model.fit(x=X_train, y=y_train, batch_size=batch_size, epochs=iteration_times, verbose=2,
                    validation_data=(X_dev, y_dev),
                    callbacks=[TensorBoard(log_dir='log/LSTM'), csv_logger],
                    shuffle=False)
filename = localtime + '.h5'
model.save(filename)
y_pred = model.predict(X_test, batch_size=1, verbose=0)
y_spec_pred = model.predict(special_test, batch_size=1, verbose=0)
with open(txt_name, 'w', newline='\n') as f:
    f.write('hypermeters:\n')
    f.write('optimizer: {}\n'.format(optimizer))
    f.write('learning_rate: {}\n'.format(learning_rate))
    f.write('decay: {}\n'.format(decay))
    f.write('momentum: {}\n'.format(momentum))
    f.write(
        'weight initialized with min={} max={}\n'.format(layer_weight_initializer_min, layer_weight_initializer_max))
    f.write('activation function is ' + activation_method + '\n')
    # f.write('regularation method is {}, lambda is {}\n'.format(weight_regularizer_type,weight_regularizer))
    f.write('-------------------------------\n')
    f.write('model parameters:\n')
    f.write('layer numbers {}\n'.format(layer_amount))
    f.write('batch size {}\n'.format(batch_size))
    f.write('iteration times {}\n'.format(iteration_times))
    f.write('result: {}\n'.format(mean_squared_error(y_test, y_pred)))
    f.write('special result: {}\n'.format(mean_squared_error(special_result, y_spec_pred)))
    for i in range(len(special_result)):
        print('true value is: {}, predictd value is {}\r\n'.format(special_result[i], y_spec_pred[i]))

print(model.summary())
