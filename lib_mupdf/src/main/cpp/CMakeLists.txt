cmake_minimum_required(VERSION 3.10.2)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DHAVE_PTHREADS")
set(CMAKE_BUILD_TYPE "Release") #设置编译Release 还是Debug版本(Release Debug)

###############################################################################################

if (${ANDROID_ABI} STREQUAL "x86_64")
    include_directories(${ANDROID_SYSROOT}/usr/include/x86_64-linux-android)
elseif (${ANDROID_ABI} STREQUAL "x86")
    include_directories(${ANDROID_SYSROOT}/usr/include/i686-linux-android)
elseif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif (${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else ()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif ()

###############################################################################################

add_definitions(-ffunction-sections -fdata-sections)
add_definitions(-D_FILE_OFFSET_BITS=32)
add_definitions(-DTOFU_NOTO -DTOFU_CJK)
add_definitions(-DAA_BITS=8)
add_definitions(-DOPJ_STATIC -DOPJ_HAVE_INTTYPES_H -DOPJ_HAVE_STDINT_H)
add_definitions(-DHAVE_LCMS2MT)

include_directories(include)
include_directories(include/mupdf)
include_directories(scripts/freetype)
include_directories(scripts/libjpeg)
include_directories(thirdparty/freetype/include)
include_directories(thirdparty/freetype/include/freetype)
include_directories(thirdparty/freetype/include/freetype/config)
include_directories(thirdparty/freetype/include/freetype/internal)
include_directories(thirdparty/freetype/include/freetype/internal/services)
include_directories(thirdparty/gumbo-parser/src)
include_directories(thirdparty/harfbuzz/src)
include_directories(thirdparty/jbig2dec)
include_directories(thirdparty/libjpeg)
include_directories(thirdparty/lcms2/include)
include_directories(thirdparty/mujs)
include_directories(thirdparty/openjpeg/src/lib/openjp2)
include_directories(thirdparty/zlib)

file(GLOB SRC_FITZ source/fitz/*.c)
file(GLOB SRC_PDF source/pdf/*.c)
file(GLOB SRC_XPS source/xps/*.c)
file(GLOB SRC_SVG source/svg/*.c)
file(GLOB SRC_CBZ source/cbz/*.c)
file(GLOB SRC_HTML source/html/*.c)
file(GLOB SRC_PKCS7 source/helpers/pkcs7/*.c)
file(GLOB SRC_URW generated/resources/fonts/urw/*.c)
file(GLOB SRC_SIL generated/resources/fonts/sil/*.c)

add_library(

        # Sets the name of the library.
        mupdf_core

        # Sets the library as a shared library.
        STATIC

        # Provides a relative path to your source file(s).
        ${SRC_FITZ}
        ${SRC_PDF}
        ${SRC_XPS}
        ${SRC_SVG}
        ${SRC_CBZ}
        ${SRC_HTML}
        ${SRC_PKCS7}
        ${SRC_URW}
        ${SRC_SIL})

###############################################################################################

add_definitions(-ffunction-sections -fdata-sections)
add_definitions(-DFT2_BUILD_LIBRARY -DDARWIN_NO_CARBON)
add_definitions("-DFT_CONFIG_MODULES_H=<slimftmodules.h>")
add_definitions("-DFT_CONFIG_OPTIONS_H=<slimftoptions.h>")
add_definitions(-DHAVE_STDINT_H)
add_definitions(-DHAVE_FREETYPE)
add_definitions(-DOPJ_STATIC -DOPJ_HAVE_INTTYPES_H -DOPJ_HAVE_STDINT_H)
add_definitions(-ffunction-sections -fdata-sections)
add_definitions(-fno-rtti -fno-exceptions -fvisibility-inlines-hidden)
add_definitions(-DHAVE_FALLBACK=1 -DHAVE_OT -DHAVE_UCDN -DHB_NO_MT)
add_definitions(-Dhb_malloc_impl=fz_hb_malloc)
add_definitions(-Dhb_calloc_impl=fz_hb_calloc)
add_definitions(-Dhb_realloc_impl=fz_hb_realloc)
add_definitions(-Dhb_free_impl=fz_hb_free)

include_directories(include)
include_directories(include/mupdf)
include_directories(scripts/freetype)
include_directories(scripts/libjpeg)
include_directories(thirdparty/freetype/include)
include_directories(thirdparty/freetype/include/freetype)
include_directories(thirdparty/freetype/include/freetype/config)
include_directories(thirdparty/freetype/include/freetype/internal)
include_directories(thirdparty/freetype/include/freetype/internal/services)
include_directories(thirdparty/gumbo-parser/src)
include_directories(thirdparty/harfbuzz/src)
include_directories(thirdparty/jbig2dec)
include_directories(thirdparty/libjpeg)
include_directories(thirdparty/lcms2/include)
include_directories(thirdparty/mujs)
include_directories(thirdparty/openjpeg/src/lib/openjp2)
include_directories(thirdparty/zlib)


add_library(

        # Sets the name of the library.
        mupdf_thirdparty

        # Sets the library as a shared library.
        STATIC

        # Provides a relative path to your source file(s).
        thirdparty/freetype/src/base/ftbase.c
        thirdparty/freetype/src/base/ftbbox.c
        thirdparty/freetype/src/base/ftbitmap.c
        thirdparty/freetype/src/base/ftdebug.c
        thirdparty/freetype/src/base/ftgasp.c
        thirdparty/freetype/src/base/ftglyph.c
        thirdparty/freetype/src/base/ftinit.c
        thirdparty/freetype/src/base/ftstroke.c
        thirdparty/freetype/src/base/ftsynth.c
        thirdparty/freetype/src/base/ftsystem.c
        thirdparty/freetype/src/base/fttype1.c
        thirdparty/freetype/src/cff/cff.c
        thirdparty/freetype/src/cid/type1cid.c
        thirdparty/freetype/src/psaux/psaux.c
        thirdparty/freetype/src/pshinter/pshinter.c
        thirdparty/freetype/src/psnames/psnames.c
        thirdparty/freetype/src/raster/raster.c
        thirdparty/freetype/src/sfnt/sfnt.c
        thirdparty/freetype/src/smooth/smooth.c
        thirdparty/freetype/src/truetype/truetype.c
        thirdparty/freetype/src/type1/type1.c

        thirdparty/gumbo-parser/src/attribute.c
        thirdparty/gumbo-parser/src/char_ref.c
        thirdparty/gumbo-parser/src/error.c
        thirdparty/gumbo-parser/src/parser.c
        thirdparty/gumbo-parser/src/string_buffer.c
        thirdparty/gumbo-parser/src/string_piece.c
        thirdparty/gumbo-parser/src/tag.c
        thirdparty/gumbo-parser/src/tokenizer.c
        thirdparty/gumbo-parser/src/utf8.c
        thirdparty/gumbo-parser/src/util.c
        thirdparty/gumbo-parser/src/vector.c

        thirdparty/harfbuzz/src/hb-aat-layout.cc
        thirdparty/harfbuzz/src/hb-aat-map.cc
        thirdparty/harfbuzz/src/hb-blob.cc
        thirdparty/harfbuzz/src/hb-buffer-serialize.cc
        thirdparty/harfbuzz/src/hb-buffer.cc
        thirdparty/harfbuzz/src/hb-common.cc
        thirdparty/harfbuzz/src/hb-face.cc
        thirdparty/harfbuzz/src/hb-fallback-shape.cc
        thirdparty/harfbuzz/src/hb-font.cc
        thirdparty/harfbuzz/src/hb-ft.cc
        thirdparty/harfbuzz/src/hb-map.cc
        thirdparty/harfbuzz/src/hb-number.cc
        thirdparty/harfbuzz/src/hb-ot-cff1-table.cc
        thirdparty/harfbuzz/src/hb-ot-cff2-table.cc
        thirdparty/harfbuzz/src/hb-ot-color.cc
        thirdparty/harfbuzz/src/hb-ot-face.cc
        thirdparty/harfbuzz/src/hb-ot-font.cc
        thirdparty/harfbuzz/src/hb-ot-layout.cc
        thirdparty/harfbuzz/src/hb-ot-map.cc
        thirdparty/harfbuzz/src/hb-ot-math.cc
        thirdparty/harfbuzz/src/hb-ot-meta.cc
        thirdparty/harfbuzz/src/hb-ot-metrics.cc
        thirdparty/harfbuzz/src/hb-ot-name.cc
        thirdparty/harfbuzz/src/hb-ot-shape.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-arabic.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-default.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-hangul.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-hebrew.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-indic-table.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-indic.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-khmer.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-myanmar.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-thai.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-use.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-use-table.cc
        thirdparty/harfbuzz/src/hb-ot-shape-complex-vowel-constraints.cc
        thirdparty/harfbuzz/src/hb-ot-shape-fallback.cc
        thirdparty/harfbuzz/src/hb-ot-shape-normalize.cc
        thirdparty/harfbuzz/src/hb-ot-tag.cc
        thirdparty/harfbuzz/src/hb-ot-var.cc
        thirdparty/harfbuzz/src/hb-set.cc
        thirdparty/harfbuzz/src/hb-shape-plan.cc
        thirdparty/harfbuzz/src/hb-shape.cc
        thirdparty/harfbuzz/src/hb-shaper.cc
        thirdparty/harfbuzz/src/hb-static.cc
        thirdparty/harfbuzz/src/hb-subset.cc
        thirdparty/harfbuzz/src/hb-subset-cff1.cc
        thirdparty/harfbuzz/src/hb-subset-cff2.cc
        thirdparty/harfbuzz/src/hb-subset-cff-common.cc
        thirdparty/harfbuzz/src/hb-subset-input.cc
        thirdparty/harfbuzz/src/hb-subset-plan.cc
        thirdparty/harfbuzz/src/hb-ucd.cc
        thirdparty/harfbuzz/src/hb-unicode.cc

        thirdparty/jbig2dec/jbig2.c
        thirdparty/jbig2dec/jbig2_arith.c
        thirdparty/jbig2dec/jbig2_arith_iaid.c
        thirdparty/jbig2dec/jbig2_arith_int.c
        thirdparty/jbig2dec/jbig2_generic.c
        thirdparty/jbig2dec/jbig2_halftone.c
        thirdparty/jbig2dec/jbig2_huffman.c
        thirdparty/jbig2dec/jbig2_hufftab.c
        thirdparty/jbig2dec/jbig2_image.c
        thirdparty/jbig2dec/jbig2_mmr.c
        thirdparty/jbig2dec/jbig2_page.c
        thirdparty/jbig2dec/jbig2_refinement.c
        thirdparty/jbig2dec/jbig2_segment.c
        thirdparty/jbig2dec/jbig2_symbol_dict.c
        thirdparty/jbig2dec/jbig2_text.c

        thirdparty/libjpeg/jaricom.c
        thirdparty/libjpeg/jcomapi.c
        thirdparty/libjpeg/jdapimin.c
        thirdparty/libjpeg/jdapistd.c
        thirdparty/libjpeg/jdarith.c
        thirdparty/libjpeg/jdatadst.c
        thirdparty/libjpeg/jdatasrc.c
        thirdparty/libjpeg/jdcoefct.c
        thirdparty/libjpeg/jdcolor.c
        thirdparty/libjpeg/jddctmgr.c
        thirdparty/libjpeg/jdhuff.c
        thirdparty/libjpeg/jdinput.c
        thirdparty/libjpeg/jdmainct.c
        thirdparty/libjpeg/jdmarker.c
        thirdparty/libjpeg/jdmaster.c
        thirdparty/libjpeg/jdmerge.c
        thirdparty/libjpeg/jdpostct.c
        thirdparty/libjpeg/jdsample.c
        thirdparty/libjpeg/jdtrans.c
        thirdparty/libjpeg/jerror.c
        thirdparty/libjpeg/jfdctflt.c
        thirdparty/libjpeg/jfdctfst.c
        thirdparty/libjpeg/jfdctint.c
        thirdparty/libjpeg/jidctflt.c
        thirdparty/libjpeg/jidctfst.c
        thirdparty/libjpeg/jidctint.c
        thirdparty/libjpeg/jmemmgr.c
        thirdparty/libjpeg/jquant1.c
        thirdparty/libjpeg/jquant2.c
        thirdparty/libjpeg/jutils.c

        thirdparty/lcms2/src/cmsalpha.c
        thirdparty/lcms2/src/cmscam02.c
        thirdparty/lcms2/src/cmscgats.c
        thirdparty/lcms2/src/cmscnvrt.c
        thirdparty/lcms2/src/cmserr.c
        thirdparty/lcms2/src/cmsgamma.c
        thirdparty/lcms2/src/cmsgmt.c
        thirdparty/lcms2/src/cmshalf.c
        thirdparty/lcms2/src/cmsintrp.c
        thirdparty/lcms2/src/cmsio0.c
        thirdparty/lcms2/src/cmsio1.c
        thirdparty/lcms2/src/cmslut.c
        thirdparty/lcms2/src/cmsmd5.c
        thirdparty/lcms2/src/cmsmtrx.c
        thirdparty/lcms2/src/cmsnamed.c
        thirdparty/lcms2/src/cmsopt.c
        thirdparty/lcms2/src/cmspack.c
        thirdparty/lcms2/src/cmspcs.c
        thirdparty/lcms2/src/cmsplugin.c
        thirdparty/lcms2/src/cmsps2.c
        thirdparty/lcms2/src/cmssamp.c
        thirdparty/lcms2/src/cmssm.c
        thirdparty/lcms2/src/cmstypes.c
        thirdparty/lcms2/src/cmsvirt.c
        thirdparty/lcms2/src/cmswtpnt.c
        thirdparty/lcms2/src/cmsxform.c

        thirdparty/mujs/one.c

        thirdparty/openjpeg/src/lib/openjp2/bio.c
        thirdparty/openjpeg/src/lib/openjp2/cio.c
        thirdparty/openjpeg/src/lib/openjp2/dwt.c
        thirdparty/openjpeg/src/lib/openjp2/event.c
        thirdparty/openjpeg/src/lib/openjp2/function_list.c
        thirdparty/openjpeg/src/lib/openjp2/image.c
        thirdparty/openjpeg/src/lib/openjp2/invert.c
        thirdparty/openjpeg/src/lib/openjp2/j2k.c
        thirdparty/openjpeg/src/lib/openjp2/jp2.c
        thirdparty/openjpeg/src/lib/openjp2/mct.c
        thirdparty/openjpeg/src/lib/openjp2/mqc.c
        thirdparty/openjpeg/src/lib/openjp2/openjpeg.c
        thirdparty/openjpeg/src/lib/openjp2/pi.c
        thirdparty/openjpeg/src/lib/openjp2/sparse_array.c
        thirdparty/openjpeg/src/lib/openjp2/t1.c
        thirdparty/openjpeg/src/lib/openjp2/t2.c
        thirdparty/openjpeg/src/lib/openjp2/tcd.c
        thirdparty/openjpeg/src/lib/openjp2/tgt.c
        thirdparty/openjpeg/src/lib/openjp2/thread.c

        thirdparty/zlib/adler32.c
        thirdparty/zlib/compress.c
        thirdparty/zlib/crc32.c
        thirdparty/zlib/deflate.c
        thirdparty/zlib/inffast.c
        thirdparty/zlib/inflate.c
        thirdparty/zlib/inftrees.c
        thirdparty/zlib/trees.c
        thirdparty/zlib/uncompr.c
        thirdparty/zlib/zutil.c)

################################################################################################

add_definitions(-DHAVE_ANDROID)

include_directories(include)
include_directories(include/mupdf)
include_directories(scripts/freetype)
include_directories(scripts/libjpeg)
include_directories(thirdparty/freetype/include)
include_directories(thirdparty/freetype/include/freetype)
include_directories(thirdparty/freetype/include/freetype/config)
include_directories(thirdparty/freetype/include/freetype/internal)
include_directories(thirdparty/freetype/include/freetype/internal/services)
include_directories(thirdparty/gumbo-parser/src)
include_directories(thirdparty/harfbuzz/src)
include_directories(thirdparty/jbig2dec)
include_directories(thirdparty/libjpeg)
include_directories(thirdparty/lcms2/include)
include_directories(thirdparty/mujs)
include_directories(thirdparty/openjpeg/src/lib/openjp2)
include_directories(thirdparty/zlib)

#file(GLOB SRC_JNI platform/java/jni/*.c)
#file(GLOB SRC_ANDROID platform/java/jni/android/*.c)

add_library(

        # Sets the name of the library.
        mupdf_java

        # Sets the library as a shared library.
        SHARED

#        ${SRC_FITZ}
#        ${SRC_ANDROID}

        # Provides a relative path to your source file(s).
        platform/java/mupdf_native.c)

target_link_libraries(

        # Specifies the target library.
        mupdf_java

        # Links the target library to the log library
        jnigraphics
        log
        m

        # static .a
        mupdf_core
        mupdf_thirdparty)
