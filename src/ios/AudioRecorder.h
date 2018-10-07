
#import <Cordova/CDV.h>

@interface AudioRecorder : CDVPlugin <AVAudioRecorderDelegate>{
    // Member variables go here.
    AVAudioSession *avSession;
    AVAudioRecorder *recorder;
    AVPlayer *avPlayer;
    NSDictionary *setting;
    NSString *resourcePath;
    NSString *mp3FilePath;
    NSString *stopRecordCallbackId;
    
    int outBitRate;
    int outSamplingRate;
    BOOL isChatMode;
    BOOL isSave;
    CFTimeInterval startRecordTime;
    CFTimeInterval endRecordTime;
    BOOL isRecording;
    
}

@property (nonatomic, strong) AVAudioSession* avSession;
@property (nonatomic, strong) AVAudioRecorder* recorder;
@property (nonatomic, strong) NSString* resourcePath;
@property (nonatomic, strong) NSString* mp3FilePath;
@property (nonatomic, strong) NSDictionary* setting;
@property (nonatomic, strong) NSString* playSoundCallbackId;
@property (nonatomic) int outBitRate;
@property (nonatomic) int outSampingRate;
@property (nonatomic) BOOL isChatMode;
@property (nonatomic) BOOL isSave;
@property (nonatomic) BOOL isRecording;


- (void) startRecord:(CDVInvokedUrlCommand*) command;
- (void) stopRecord:(CDVInvokedUrlCommand*) command;
- (void) hasPermission: (CDVInvokedUrlCommand*) command;
- (void) requestPermission: (CDVInvokedUrlCommand*) command;
- (void) playSound: (CDVInvokedUrlCommand*) command;
- (void) stopSound: (CDVInvokedUrlCommand*) command;
@end
