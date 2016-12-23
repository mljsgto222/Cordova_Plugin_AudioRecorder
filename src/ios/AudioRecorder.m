/********* AudioRecorder.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <AVFoundation/AVFoundation.h>

#import "AudioRecorder.h"
#import "SimpleLame.h"

#define DEFAULT_OUT_BIT_RATE 16
#define DEFAULT_OUT_SAMPLING_RATE 44100
#define IN_SAMPLING_RATE 44100
#define PCM_SIZE 8192

#define OUT_SAMPLING_RATE @"outSamplingRate"
#define OUT_BIT_RATE @"outBitRate"
#define TEMP_FILE_NAME @"temp"


@implementation AudioRecorder
@synthesize setting, avSession, documentDirectory, recorder, resourcePath, outBitRate, outSampingRate, isRecording;

- (void) pluginInitialize{
    documentDirectory = NSTemporaryDirectory();
    outSamplingRate = DEFAULT_OUT_SAMPLING_RATE;
    outBitRate = DEFAULT_OUT_BIT_RATE;
    setting = [NSDictionary dictionaryWithObjectsAndKeys:
               [NSNumber numberWithInt:kAudioFormatLinearPCM], AVFormatIDKey,
               [NSNumber numberWithInt:IN_SAMPLING_RATE], AVSampleRateKey,
               [NSNumber numberWithInt:16], AVLinearPCMBitDepthKey,
               [NSNumber numberWithInt:2], AVNumberOfChannelsKey,
               [NSNumber numberWithInt:kAudioConverterQuality_Max], AVEncoderAudioQualityKey,
               nil];

}

- (void) startRecord:(CDVInvokedUrlCommand *)command
{
    if([recorder isRecording]){
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    NSDictionary *options = [command.arguments objectAtIndex:0];
    if(options != nil){
        if([options valueForKey:OUT_SAMPLING_RATE] != nil){
            outSamplingRate = [[options valueForKey:OUT_SAMPLING_RATE] intValue];
        }
        if([options valueForKey: OUT_BIT_RATE] != nil){
            outBitRate = [[options valueForKey:OUT_BIT_RATE] intValue];
        }
    }
    
    __weak AudioRecorder* recorderSelf = self;
    __weak NSString* weakCallbackId = [command callbackId];
    
    void (^startRecording)(void) = ^{
        __strong  AudioRecorder* audioRecorder = recorderSelf;
        __strong NSString* callbackId = weakCallbackId;
        CDVPluginResult *pluginResult = nil;
        NSError* __autoreleasing error = nil;
        
        if(audioRecorder.recorder != nil){
            [audioRecorder.recorder stop];
            audioRecorder.recorder = nil;
        }
        
        if([audioRecorder hasAvSession]){
            if(![audioRecorder.avSession.category isEqualToString:AVAudioSessionCategoryPlayAndRecord]){
                [audioRecorder.avSession setCategory:AVAudioSessionCategoryPlayAndRecord withOptions:AVAudioSessionCategoryOptionDefaultToSpeaker error:nil];
                [audioRecorder.avSession setMode:AVAudioSessionModeVoiceChat error:nil];
            }

            if(![audioRecorder.avSession setActive:YES error:&error]){
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: [NSString stringWithFormat:@"Unable to record audio: %@", [[error userInfo] description]]];
                [audioRecorder.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
                return;
            }
        }
        
        audioRecorder.resourcePath = [audioRecorder.documentDirectory stringByAppendingFormat:@"%@.pcm", TEMP_FILE_NAME];
        NSURL *url = [NSURL URLWithString:audioRecorder.resourcePath];
        audioRecorder.recorder = [[AVAudioRecorder alloc] initWithURL:url settings:audioRecorder.setting error:&error];
        
        bool recordingSuccess = NO;
        if (error == nil){
            audioRecorder.recorder.delegate = audioRecorder;
            audioRecorder.recorder.meteringEnabled = YES;
            recordingSuccess = [audioRecorder.recorder record];
            
            if(recordingSuccess){
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [audioRecorder.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
                isRecording = YES;
                startRecordTime = CACurrentMediaTime();
                [NSTimer scheduledTimerWithTimeInterval:.2 target:self selector:@selector(convertMp3) userInfo:nil repeats:NO];
                return;
            }
        }
        
        if(error != nil || !recordingSuccess){
            if(error != nil){
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: [NSString stringWithFormat: @"Failed to initialize AvAudioRecorder: %@", [[error userInfo] description]]];
            }else{
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to start recording"];
            }
            audioRecorder.recorder = nil;
            if([audioRecorder hasAvSession]){
                [audioRecorder.avSession setActive:NO error:nil];
            }
            [audioRecorder.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
            return;
        }
    };
    
    SEL rrpSel = NSSelectorFromString(@"requestRecordPermission:");
    if([self hasAvSession] && [self.avSession respondsToSelector:rrpSel]){
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        [self.avSession performSelector:rrpSel withObject:^(BOOL granted){
            __strong AudioRecorder* audioRecorder = recorderSelf;
            __strong NSString* callbackId = weakCallbackId;
            if(granted){
                startRecording();
            } else {
                audioRecorder.recorder = nil;
                if([audioRecorder hasAvSession]){
                    [audioRecorder.avSession setActive:NO error:nil];
                }
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Microphone permission denied."];
                [audioRecorder.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
            }
        }];
#pragma clang diagnostic pop
    }else{
        startRecording();
    }
    
}

- (void) stopRecord:(CDVInvokedUrlCommand *)command
{
    stopRecordCallbackId = command.callbackId;
    if(recorder != nil){
        [recorder stop];
        recorder = nil;
    }
}

- (void) audioRecorderDidFinishRecording:(AVAudioRecorder *)recorder successfully:(BOOL)flag
{
    if(flag){
        
    }else{
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to encoding audio"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:stopRecordCallbackId];
    }
    
    if([self hasAvSession]){
        if(![avSession.category isEqualToString:AVAudioSessionCategoryAmbient]){
            [self.avSession setCategory:AVAudioSessionCategoryAmbient error:nil];
            [self.avSession setMode:AVAudioSessionModeDefault error:nil];
        }
        [self.avSession setActive:NO error: nil];
    }
    isRecording = NO;
    endRecordTime = CACurrentMediaTime();
}

- (void)convertMp3
{
    __weak AudioRecorder* weakRecorder = self;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        __strong AudioRecorder* audioRecorder = weakRecorder;
        if(audioRecorder){
            NSString *mp3FilePath = [audioRecorder.documentDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.mp3", TEMP_FILE_NAME]];
            BOOL convertMp3Success = NO;
            @try{
                int read, write;
                
                FILE *mp3File = fopen([mp3FilePath cStringUsingEncoding:1], "wb");
                FILE *pcmFile = fopen([audioRecorder.resourcePath cStringUsingEncoding:1], "rb");
                
                short int pcmBuffer[PCM_SIZE *2];
                unsigned char mp3Buffer[PCM_SIZE];
                
                [SimpleLame init:IN_SAMPLING_RATE outSamplerate:audioRecorder.outSampingRate outChannel:1 outBitrate:audioRecorder.outBitRate];
                
                long curPos;
                BOOL hasSkipHeader = NO;
                
                do{
                    curPos = ftell(pcmFile);
                    long startPos = ftell(pcmFile);
                    
                    fseek(pcmFile, 0, SEEK_END);
                    long endPos = ftell(pcmFile);
                    
                    long length = endPos - startPos;
                    fseek(pcmFile, curPos, SEEK_SET);
                    
                    if(length > PCM_SIZE * 2 * sizeof(short int)){
                        if(!hasSkipHeader){
                            fseek(pcmFile, 4 * 1024, SEEK_SET);
                            hasSkipHeader = YES;
                        }
                        
                        read = fread(pcmBuffer, 2 * sizeof(short int), PCM_SIZE, pcmFile);
                        write = [SimpleLame encode:pcmBuffer samples: read mp3buf:mp3Buffer mp3bufSize:PCM_SIZE];
                        if(write >= 0){
                            fwrite(mp3Buffer, write, 1, mp3File);
                        }
                    }
                } while (audioRecorder.isRecording);
                do{
                    read = fread(pcmBuffer, 2 * sizeof(short int), PCM_SIZE, pcmFile);
                    if(read == 0){
                        write = [SimpleLame flush:mp3Buffer mp3bufSize:PCM_SIZE];
                    }else{
                        write = [SimpleLame encode:pcmBuffer samples: read mp3buf:mp3Buffer mp3bufSize:PCM_SIZE];
                    }
                }while (read != 0);
                
                
                [SimpleLame close];
                fclose(mp3File);
                fclose(pcmFile);
                convertMp3Success = YES;
            
            }
            @catch(NSException *ex){
                NSLog(@"Failed to converting audio: %@", [[ex userInfo] description]);
            }
            @finally{
                [audioRecorder convertMp3Finished: convertMp3Success];
            }
        }
    });
}

- (void)convertMp3Finished:(BOOL)flag
{
    CDVPluginResult *pluginResult;
    if(flag){
        NSString *mp3FileName = [NSString stringWithFormat:@"%@.mp3", TEMP_FILE_NAME];
        NSDictionary *result = [NSDictionary dictionaryWithObjectsAndKeys:
                                mp3FileName, @"name",
                                @"audio/mpeg", @"type",
                                [documentDirectory stringByAppendingPathComponent:mp3FileName], @"uri",
                                [NSString stringWithFormat:@"%.2g", endRecordTime - startRecordTime ], @"duration",
                                nil];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to converting mp3"];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:stopRecordCallbackId];
    
}

- (BOOL) hasAvSession
{
    BOOL hasSession = YES;
    if(avSession == nil){
        NSError *error = nil;
        
        avSession = [AVAudioSession sharedInstance];
        if(error != nil){
            NSLog(@"get AvAudioSession failed: %@", [[error userInfo] description]);
            avSession = nil;
            hasSession = NO;
        }
    }
    return hasSession;
}

@end
