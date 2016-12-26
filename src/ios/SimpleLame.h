
@interface SimpleLame

+ (int)init:(int)inSamplerate outSamplerate:(int)outSamplerate outChannel:(int)outChannel outBitrate:(int)outBitrate scale:(float)scale;
+ (int)encode:(short int *)buffer samples:(int)samples mp3buf:(unsigned char *)mp3buf mp3bufSize:(int)mp3bufSize;
+ (int)flush:(unsigned char *)mp3buf mp3bufSize:(int)mp3bufSize;
+ (void)close;

@end
