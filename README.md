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
    - `file`: output mp3 file object
        - `name`: mp3 file's name.(_string_)
        - `type`: `'audio/mpeg'`(_string_)
        - `uri`: file local uri(_string_)

- __error__: stopRecord error callback
    - 'errorMessage': a string about error(_string_)

## Reference

[AndroidMp3Recorder](https://github.com/telescreen/AndroidMp3Recorder)
[cordova-plugin-media](https://cordova.apache.org/docs/en/latest/reference/cordova-plugin-media/)

## License

Copyright (C) 2016 Zhengz

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.