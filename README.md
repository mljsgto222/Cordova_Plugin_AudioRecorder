# Cordova_Plugin_AudioRecorder

This is a cordova plugin for take audio record. And the record will be compressed into MP3 format.

## Supported Platforms

- iOS >= 6.0
- Android >= 4.0

## Usage

### Samplingrates

```
AudioRecorder.AUDIO_SAMPLINGS = {
    MAX: 44100,
    NORMAL: 22050,
    LOW: 8000
}
```

### Start Record

```
AudioRecorder.startRecord(options, success, error);
```

- __options__: recorder settings
    - `outSamplingRate`: set sampling rate for output mp3. _default: 22050_(note: lame convert mp3 may failed when use other samplingrates.)(_number_)
    - `outBitRate`: set bit rate for output mp3. _default: 16_(_number_)

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

## License

Copyright (C) 2016 Zhengz

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.