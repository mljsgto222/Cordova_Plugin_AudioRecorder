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

### Start Record

```
AudioRecorder.startRecord(options, success, error);
```

- __options__: recorder settings
    - `outSamplingRate`: set sampling rate for output mp3. _default: 22050(_number_)
    - `outBitRate`: set bit rate for output mp3. _default: 16(_number_)

- __success__: startRecord success callback.

- __error__: startRecord error callback.
    - `errorMessage`: a string about error(_string_)

### Stop Record

```
AudioRecorder.stopRecord(success, error)
```

- __success__: stopRecord success callback.
    - `file`: output mp3 file object(This is a temporary file on device)
        - `name`: mp3 file's name.(_string_)
        - `type`: `'audio/mpeg'`(_string_)
        - `uri`: file local uri(_string_)

- __error__: stopRecord error callback
    - 'errorMessage': a string about error(_string_)

## TODO LIST

- Set duration for audio recording. 
- Save record in device storage.
- Play audio on device or Internet

## Reference

[AndroidMp3Recorder](https://github.com/telescreen/AndroidMp3Recorder)
[cordova-plugin-media](https://cordova.apache.org/docs/en/latest/reference/cordova-plugin-media/)