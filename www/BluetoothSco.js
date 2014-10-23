var exec = require('cordova/exec'),
    BluetoothSco = function() {};
    
BluetoothSco.prototype.start = function(onSuccess, onError) {
    return exec(onSuccess, onError, 'BluetoothSco', 'start', []);
};

BluetoothSco.prototype.stop = function(onSuccess, onError) {
    return exec(onSuccess, onError, 'BluetoothSco', 'stop', []);
};

module.exports = new BluetoothSco();
