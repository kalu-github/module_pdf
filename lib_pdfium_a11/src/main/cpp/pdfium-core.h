//
// Created by yangl on 2017/6/9.
//
//

#ifndef _UTIL_HPP_
#define _UTIL_HPP_

#include <jni.h>

extern "C" {
#include <stdlib.h>
}

#include <android/log.h>

#define JIN_PASSWORD_EXCEPTION "lib/kalu/pdfium/PdfPasswordException"
#define JNI_FUNC(retType, bindClass, name)  JNIEXPORT retType JNICALL Java_lib_kalu_pdfium_##bindClass##_##name
#define JNI_ARGS    JNIEnv *env, jobject thiz

#define LOG_TAG "pdfium_v11"
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif // _UTIL_HPP_
