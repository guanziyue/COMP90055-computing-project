import tensorflow as tf
import keras
from keras.initializers import Constant
from keras.layers.core import Dense, Activation, Lambda
from keras.models import Sequential
from keras.initializers import RandomUniform
from keras.callbacks import TensorBoard
from keras.optimizers import SGD
from keras.regularizers import l1
from keras.optimizers import Adam
import numpy as np
import pandas as pd
import time
import keras.backend.tensorflow_backend as KTF
KTF.set_session(tf.Session(config=tf.ConfigProto(device_count={'cpu':1})))

def get_data(path):
    dateparse = lambda dates: pd.datetime.strptime(dates, '%Y-%m-%d %H:%M:%S')
    dataset = pd.read_csv(path, parse_dates=[0], date_parser=dateparse)
    # remove outliers which value>80. it only 8 piece in 64639
    dataset = dataset[dataset.loc[:, 'value'] < 80]
    # X_RNN=dataset.ix[:,0:-1]
    # X_RNN_column=X_RNN.columns
    X_normal = dataset.ix[:, 1:-1]
    X_normal_column = X_normal.columns
    y = dataset.ix[:, ['value']]
    # X_RNN=X_RNN.values
    X_normal = X_normal.values
    y = y.values
    y = y.reshape((1, -1))[0]
    return X_normal, y, X_normal_column


# do feature scaling because some value is too large. To accelerate training also avoid gradient vanish


from sklearn.model_selection import train_test_split

# get training set and test set. it has 87 features (time not including)
X, y, column = get_data('train_set.csv')
y = y / 80
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1)
X_dev, y_dev, column = get_data('test_set.csv')
X_min = np.min(X_train, axis=0)
X_max = np.max(X_train, axis=0)


def feature_scaling(input):
    output = (input - X_min) / (X_max - X_min)
    return output


#X_dev = feature_scaling(X_dev)
y_dev = y_dev / 80
# hypermeters
optimizer = 'sgd'
learning_rate = 0.01
decay = 1e-6
momentum = 0.8
layer_weight_initializer_min = 0.005
layer_weight_initializer_max = 0.015
bias_initializer = 0.001
sgd = SGD(lr=learning_rate, decay=decay, momentum=momentum, nesterov=True)
activation_function = 'relu'
#weight_regularizer_type = 'l1'
#weight_regularizer = 0.001
layer_amount = 3
batch_size = 3200
iteration_times = 40000

# record parameters
localtime = time.strftime('%Y-%m-%d_%H%M%S', time.localtime(time.time()))
txt_name=str(localtime+'.txt')
# hypermeters objects
random_initializer = RandomUniform(minval=layer_weight_initializer_min, maxval=layer_weight_initializer_max)
bias_init = Constant(value=bias_initializer)

# models
model = Sequential()
model.add(Lambda(feature_scaling, input_shape=(87,)))
model.add(
    Dense(87, use_bias=True, kernel_initializer=random_initializer, activation=activation_function,
          bias_initializer=bias_init))
model.add(
    Dense(87, use_bias=True, kernel_initializer=random_initializer, activation=activation_function,
          bias_initializer=bias_init))
model.add(
    Dense(87, use_bias=True, kernel_initializer=random_initializer, activation=activation_function,
          bias_initializer=bias_init))
model.add(Dense(1, use_bias=False, kernel_initializer=random_initializer))
model.compile(optimizer=sgd, loss='mean_squared_error', metrics=['accuracy'])
print('training start')
model.fit(x=X_train, y=y_train, batch_size=batch_size, epochs=iteration_times, verbose=2,
          validation_data=(X_dev, y_dev),
          callbacks=[TensorBoard(log_dir='log/two_layer_sgd')])
y_pred=model.predict(X_test,batch_size=1,verbose=0)
from sklearn.metrics import mean_squared_error
with open(txt_name, 'w') as f:
    f.write('hypermeters:')
    f.write('optimizer: {}'.format(optimizer))
    f.write('learning_rate: {}'.format(learning_rate))
    f.write('decay: {}'.format(decay))
    f.write('momentum: {}'.format(momentum))
    f.write('weight initialized with min={} max={}'.format(layer_weight_initializer_min,layer_weight_initializer_max))
    f.write('bias initialized as {}'.format(bias_initializer))
    f.write('activation function is {}'.format(activation_function))
    #f.write('regularation method is {}, lambda is {}'.format(weight_regularizer_type,weight_regularizer))
    f.write('-------------------------------')
    f.write('model parameters:')
    f.write('layer numbers {}'.format(layer_amount))
    f.write('batch size {}'.format(batch_size))
    f.write('iteration times {}'.format(iteration_times))
    f.write( 'result: {}'.format(mean_squared_error(y_test, y_pred)))
filename = localtime + '.h5'
model.save(filename)
print(model.summary())
