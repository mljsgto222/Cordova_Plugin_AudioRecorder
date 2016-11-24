var exec = require('cordova/exec');

exports.startRecord = function(options, success, error) {
    exec(success, error, "AudioRecorder", "startRecord", [options]);
};

exports.stopRecord = function(success, error){
    exec(success, error, "AudioRecorder", 'stopRecord');
};
