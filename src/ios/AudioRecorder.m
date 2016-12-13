/********* AudioRecorder.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <AVFoundation/AVFoundation.h>

#import "AudioRecorder.h"
#import "SimpleLame.h"

const int DEFAULT_OUT_BIT_RATE = 16;
const int DEFAULT_OUT_SAMPLING_RATE = 44100;
const int IN_SAMPLING_RATE = 44100;
const int PCM_SIZE = 8192;

NSString const *OUT_SAMPLING_RATE= @"outSamplingRate";
NSString const *OUT_BIT_RATE = @"outBitRate";
NSString const *TEMP_FILE_NAME = @"temp";

@implementation AudioRecorder

- (void) pluginInitialize{
    documentDirectory = NSTemporaryDirectory();
    outSamplingRate = DEFAULT_OUT_SAMPLING_RATE;
    outBitRate = DEFAULT_OUT_BIT_RATE;
    setting = [NSDictionary dictionaryWithObjectsAndKeys:
               [NSNumber numberWithInt:kAudioFormatLinearPCM], AVFormatIDKey,
               [NSNumber numberWithInt:IN_SAMPLING_RATE], AVSampleRateKey,
               [NSNumber numberWithInt:16], AVLinearPCMBitDepthKey,
               [NSNumber numberWithInt:2], AVNumberOfChannelsKey,
               nil];

}

- (void) startRecord:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *pluginResult = nil;
    if([recorder isRecording]){
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"AudioRecorder has already in record"];
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
    
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    NSError *error;
    [audioSession setCategory:AVAudioSessionCategoryRecord error:&error];
    if(error != nil){
        NSLog(@"audioSession error: %@", [[error userInfo] description]);
        [self sendErrorResult:error callbackId:command.callbackId];
        return;
    }
    error = nil;
    [audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&error];
    error = nil;
    [audioSession setActive:YES error:&error];
    if(error != nil){
        NSLog(@"audioSession error: %@", [[error userInfo] description]);
        [self sendErrorResult:error callbackId:command.callbackId];
        return;
    }
    
    NSString *fileName = [NSString stringWithFormat:@"%@.pcm", TEMP_FILE_NAME];
    recordPath = [documentDirectory stringByAppendingPathComponent:fileName];
    NSURL *url = [NSURL fileURLWithPath:recordPath];
    error = nil;
    recorder = [[AVAudioRecorder alloc] initWithURL:url settings:setting error:&error];
    if(recorder == nil){
        NSLog(@"AVAudioRecorder error: %@", [[error userInfo] description]);
        [self sendErrorResult:error callbackId:command.callbackId];
        return;
    }
    
    [recorder setDelegate:self];
    [recorder prepareToRecord];
    recorder.meteringEnabled = YES;
    
    BOOL audioAvailable = audioSession.isInputAvailable;
    if(!audioAvailable){
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"audio input hardware not available"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    [recorder record];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) stopRecord:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *pluginResult;
    if(recorder != nil){
        [recorder stop];
    }
    if(recordPath != nil){
        [NSThread detachNewThreadSelector:@selector(convertMp3) toTarget:self withObject:nil];
        stopRecordCallbackId = command.callbackId;
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) convertMp3
{
    NSString *mp3FileName = [NSString stringWithFormat:@"%@.mp3", TEMP_FILE_NAME];
    NSString *mp3FilePath = [documentDirectory stringByAppendingPathComponent:mp3FileName];
    int status;
    @try{
        int read, write;
        
        FILE *pcm = fopen([recordPath cStringUsingEncoding:1], "rb");
        fseek(pcm, 4 * 1024, SEEK_CUR);
        FILE *mp3 = fopen([mp3FilePath cStringUsingEncoding:1], "wb");
        
        short int pcmBuffer[PCM_SIZE * 2];
        int mp3Size = (int)(PCM_SIZE);
        unsigned char mp3Buffer[mp3Size];
        
        int result = [SimpleLame init:IN_SAMPLING_RATE outSamplerate:outSamplingRate outChannel:1 outBitrate:outBitRate];
        if(result >= 0){
            do{
                read = fread(pcmBuffer, 2 * sizeof(short int), PCM_SIZE, pcm);
                if(read == 0){
                    write = [SimpleLame flush:mp3Buffer mp3bufSize:mp3Size];
                }else{
                    write = [SimpleLame encode:pcmBuffer samples: read mp3buf:mp3Buffer mp3bufSize:mp3Size];
                }
                if(write >= 0){
                    fwrite(mp3Buffer, write, 1, mp3);
                }
            } while (read != 0);
            
            [SimpleLame close];
            fclose(mp3);
            fclose(pcm);
            status = 0;
        }
    }
    @catch (NSException *ex){
        NSLog(@"%@", [ex description]);
        status = -1;
    }
    @finally{
        [self performSelectorOnMainThread:@selector(convertMp3Finished:) withObject: [NSNumber numberWithInt:status] waitUntilDone:YES];
    }
}

- (void)convertMp3Finished:(NSNumber *)status
{
    CDVPluginResult *pluginResult;
    if([status intValue] >= 0){
        NSString *mp3FileName = [NSString stringWithFormat:@"%@.mp3", TEMP_FILE_NAME];
        NSDictionary *result = [NSDictionary dictionaryWithObjectsAndKeys:
                                mp3FileName, @"name",
                                @"audio/mpeg", @"type",
                                [documentDirectory stringByAppendingPathComponent:mp3FileName], @"uri",
                                nil];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"convert to mp3 failed"];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:stopRecordCallbackId];
    
}

- (void) sendErrorResult:(NSError*)error callbackId:(NSString*)callbackId
{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@ %ld %@", [error domain], [error code], [[error userInfo] description]]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}


@end
