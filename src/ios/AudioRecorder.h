
#import <Cordova/CDV.h>

@interface AudioRecorder : CDVPlugin <AVAudioRecorderDelegate>{
  // Member variables go here.
    AVAudioRecorder *recorder;
    NSDictionary *setting;
    NSString *recordPath;
    NSString *documentDirectory;
    NSString *stopRecordCallbackId;

    int outBitRate;
    int outSamplingRate;
}

- (void) startRecord:(CDVInvokedUrlCommand*) command;
- (void) stopRecord:(CDVInvokedUrlCommand*) command;
@end