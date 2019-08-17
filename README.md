# Cordova_Plugin_AudioRecorder

This is a cordova plugin for audio recording. And the record will be compressed into MP3 format by [LAME](http://lame.sourceforge.net/index.php).

## Supported Platforms

- iOS >= 6.0
- Android >= 4.0

## Usage

### Constants

- `AudioRecorder.MAX`           = 1
- `AudioRecorder.NORMAL`        = 2
- `AudioRecorder.LOW`           = 3

__NOTE__: The transformation of mp3 files by LAME may fail when using other samplingrates.

### Has Permission

```
AudioRecorder.hasPermission(success, error);
```

- __success__: hasPermission success callback.
    - `hasPermission`: has audio recorder permission. (_boolean_)

- __error__:hasPermission error callback

### Request Permission

```
AudioRecorder.requestPermission(success, error);
```

### Start Record

```
AudioRecorder.startRecord(options, success, error); // auto request permission
```

- __options__: recorder settings
    - `outSamplingRate`: Set sampling rate for output mp3. default: 22050(_number_)
    - `outBitRate`: Set bit rate for output mp3. default: 16(_number_)
    - `isChatMode`: If true then set AVAudioSessionModeVoiceChat in iOS and use NoiseSuppressor in Android. default: false(_boolean_)
    - `isSave`: If true then save mp3 file into device storage. default: false(_boolean_)
    - `duration`: Set the duration of recorder file in seconds. default: 0(_number_)

- __success__: startRecord success callback.
    - `file`: output mp3 file object(This is a temporary file on device)
        - `name`: mp3 file's name.(_string_)
        - `type`: `'audio/mpeg'`(_string_)
        - `uri`: file local uri(_string_)

- __error__: startRecord error callback.
    - `errorMessage`: a string about error(_string_)

### Stop Record

```
AudioRecorder.stopRecord(success)
```

- __success__: stopRecord success callback.


### Play Sound

```
AudioRecorder.playSound(path, success, error)
```

- __path__: path for play local and remote file-base media.

- __success__: play sound success callback.(keep callback)
    - `status`: play status(start、finish、stop). (_string_)

- __error__: startRecord error callback.
    - `errorMessage`: a string about error(_string_)


### Stop Sound

```
AudioRecorder.playSound(success)
```

- __success__: stop sound success callback.(keep callback)


## TODO LIST

- Set duration for audio recording. 
- ~~Save record in device storage~~.
- ~~Play audio on device or Internet~~.

## Reference

[AndroidMp3Recorder](https://github.com/telescreen/AndroidMp3Recorder)

[cordova-plugin-media](https://cordova.apache.org/docs/en/latest/reference/cordova-plugin-media/)