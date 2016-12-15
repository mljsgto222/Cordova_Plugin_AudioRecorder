
#import <Cordova/CDV.h>

@interface AudioRecorder : CDVPlugin <AVAudioRecorderDelegate>{
  // Member variables go here.
    AVAudioSession *avSession;
    AVAudioRecorder *recorder;
    NSDictionary *setting;
    NSString *resourcePath;
    NSString *documentDirectory;
    NSString *stopRecordCallbackId;

    int outBitRate;
    int outSamplingRate;
    CFTimeInterval startRecordTime;
    CFTimeInterval endRecordTime;
    BOOL isRecording;
}

@property (nonatomic, strong) AVAudioSession* avSession;
@property (nonatomic, strong) AVAudioRecorder* recorder;
@property (nonatomic, strong) NSString* resourcePath;
@property (nonatomic, strong) NSString* documentDirectory;
@property (nonatomic, strong) NSDictionary* setting;
@property (nonatomic) int outBitRate;
@property (nonatomic) int outSampingRate;
@property (nonatomic) BOOL isRecording;

- (void) startRecord:(CDVInvokedUrlCommand*) command;
- (void) stopRecord:(CDVInvokedUrlCommand*) command;
@end