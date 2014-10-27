var exec = require('cordova/exec'),
    BluetoothSco = function() {};
    
BluetoothSco.prototype.receive = function(onSuccess, onError) {
    return exec(onSuccess, onError, 'BluetoothSco', 'receive', []);
};

BluetoothSco.prototype.stop = function(onSuccess, onError) {
    return exec(onSuccess, onError, 'BluetoothSco', 'stop', []);
};

BluetoothSco.prototype.send = function(onSuccess, onError) {
    return exec(onSuccess, onError, 'BluetoothSco', 'send', []);
};

module.exports = new BluetoothSco();
