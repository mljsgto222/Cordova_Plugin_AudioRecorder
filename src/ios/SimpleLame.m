#import "lame.h"
#import "SimpleLame.h"

@implementation SimpleLame
static lame_global_flags *glf = nil;

+ (int)init:(int)inSamplerate outSamplerate:(int)outSamplerate outChannel:(int)outChannel outBitrate:(int)outBitRate scale:(float)scale{
    if(glf != nil){
        lame_close(glf);
        glf = nil;
    }
    glf = lame_init();
    lame_set_in_samplerate(glf, inSamplerate);
    lame_set_num_channels(glf, outChannel);
    lame_set_out_samplerate(glf, outSamplerate);
    lame_set_brate(glf, outBitRate);
    lame_set_quality(glf, 7);
    lame_set_scale(glf, scale);
    int result = lame_init_params(glf);
    return result;
}

+ (int)encode:(short int *)buffer samples:(int)samples mp3buf:(unsigned char *)mp3buf mp3bufSize:(int)mp3bufSize{
    int result = lame_encode_buffer_interleaved(glf, buffer, samples, mp3buf, mp3bufSize);
    return result;
}

+ (int)flush:(unsigned char *)mp3buf mp3bufSize:(int)mp3bufSize{
    int result = lame_encode_flush(glf, mp3buf, mp3bufSize);
    return result;
}

+ (void)close{
    lame_close(glf);
    glf = nil;
}

@end
