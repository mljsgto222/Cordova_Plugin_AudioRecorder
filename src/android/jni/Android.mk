
ROOT_PATH := $(call my-dir)

# make speex and lame
APP_CFLAGS += -DSTDC_HEADERS
LOCAL_PATH := $(call my-dir)
include $(ROOT_PATH)/libmp3lame/Android.mk