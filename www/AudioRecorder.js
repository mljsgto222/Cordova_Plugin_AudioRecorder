var exec = require('cordova/exec');

var AUDIO_SAMPLINGS = {
    MAX: 44100,
    NORMAL: 22050,
    LOW: 8000
}

exports.startRecord = function(options, success, error) {
    exec(success, error, "AudioRecorder", "startRecord", [options]);
};

exports.stopRecord = function(success, error){
    exec(success, error, "AudioRecorder", 'stopRecord');
};
