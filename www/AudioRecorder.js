var exec = require('cordova/exec');

exports.AUDIO_SAMPLINGS = {
    MAX: 44100,
    NORMAL: 22050,
    LOW: 8000
}

var noop = function(){};
var isReady = false;
var readyCallbacks = [];
var onReady = function(){
    isReady = true;
    for(var i = 0; i < readyCallbacks.length; i++){
        readyCallbacks[i]();
    }
};
exports.ready = function(callback){
    callback = callback || noop;
    if(isReady){
        callback();
    }else{
        readyCallbacks.push(callback);
    }
}

exports.init = function(options, success, error){
    success = success || noop;
    exec(function(){
        onReady();
        success();
    }, error, "AudioRecorder", "init", [options]);
}

exports.startRecord = function(success, error) {
    exec(success, error, "AudioRecorder", "startRecord");
};

exports.stopRecord = function(success, error){
    exec(success, error, "AudioRecorder", 'stopRecord');
};
