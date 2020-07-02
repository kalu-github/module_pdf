cmake_minimum_required(VERSION 3.4.1)

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


# 编译库输出路径
# set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/output/${ANDROID_ABI})
# set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/output/${ANDROID_ABI})
# set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/output/${ANDROID_ABI})

#####################################       step1          #####################################
################################################################################################
###################################   mupdfcore.a   ############################################

# cFlags
add_definitions(-ffunction-sections)
add_definitions(-fdata-sections)
add_definitions(-D_FILE_OFFSET_BITS=32)
add_definitions(-DTOFU_NOTO -DTOFU_CJK)
add_definitions(-DAA_BITS=8)
add_definitions(-DOPJ_STATIC)
add_definitions(-DOPJ_HAVE_INTTYPES_H)
add_definitions(-DOPJ_HAVE_STDINT_H)
add_definitions(-DHAVE_LCMS2MT)

# 设置头文件搜索路径 - 对应于Android.mk 下的 LOCAL_C_INCLUDES
include_directories(
        src/main/cpp/include
        src/main/cpp/scripts/freetype
        src/main/cpp/scripts/libjpeg
        src/main/cpp/thirdparty/freetype/include
        src/main/cpp/thirdparty/harfbuzz/src
        src/main/cpp/thirdparty/jbig2dec
        src/main/cpp/thirdparty/libjpeg
        src/main/cpp/thirdparty/lcms2/include
        src/main/cpp/thirdparty/mujs
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2
        src/main/cpp/thirdparty/zlib)

# 源文件提供一个相对路径
file(GLOB FITZ_FILES "src/main/cpp/source/fitz/*.c")
file(GLOB PDF_FILES "src/main/cpp/source/pdf/*.c")
file(GLOB XPS_FILES "src/main/cpp/source/xps/*.c")
file(GLOB SVG_FILES "src/main/cpp/source/svg/*.c")
file(GLOB CBZ_FILES "src/main/cpp/source/cbz/*.c")
file(GLOB HTML_FILES "src/main/cpp/source/html/*.c")
file(GLOB URW_FILES "src/main/cpp/generated/resources/fonts/urw/*.c")
file(GLOB SIL_FILES "src/main/cpp/generated/resources/fonts/sil/*.c")

# mupdfcore.a
add_library(

        # Sets the name of the library.
        mupdf-core

        # Sets the library as a shared library.
        STATIC

        # Provides a relative path to your source file(s).
        ${FITZ_FILES}
        ${PDF_FILES}
        ${XPS_FILES}
        ${SVG_FILES}
        ${CBZ_FILES}
        ${HTML_FILES}
        ${URW_FILES}
        ${SIL_FILES})

###################################   mupdfcore.a   ############################################
################################################################################################

#####################################       step2          #####################################
################################################################################################
################################  mupdf_thirdparty.a  ##########################################

# cFlags
add_definitions(-ffunction-sections)
add_definitions(-fdata-sections)
add_definitions(-DFT2_BUILD_LIBRARY)
add_definitions(-DDARWIN_NO_CARBON)
add_definitions(-DFT_CONFIG_MODULES_H="slimftmodules.h")
add_definitions(-DFT_CONFIG_OPTIONS_H="slimftoptions.h")
add_definitions(-DHAVE_STDINT_H)
add_definitions(-DOPJ_STATIC)
add_definitions(-DOPJ_HAVE_INTTYPES_H)
add_definitions(-DOPJ_HAVE_STDINT_H)

# cppFlags
add_definitions(-ffunction-sections)
add_definitions(-fdata-sections)
add_definitions(-fno-rtti)
add_definitions(-fno-exceptions)
add_definitions(-fvisibility-inlines-hidden)
add_definitions(-DHAVE_FALLBACK=1)
add_definitions(-DHAVE_OT)
add_definitions(-DHAVE_UCDN)
add_definitions(-DHB_NO_MT)
add_definitions(-Dhb_malloc_impl=fz_hb_malloc)
add_definitions(-Dhb_calloc_impl=fz_hb_calloc)
add_definitions(-Dhb_realloc_impl=fz_hb_realloc)
add_definitions(-Dhb_free_impl=fz_hb_free)


# 设置头文件搜索路径 - 对应于Android.mk 下的 LOCAL_C_INCLUDES
include_directories(
        src/main/cpp/include
        src/main/cpp/include/mupdf
        src/main/cpp/scripts/freetype
        src/main/cpp/scripts/libjpeg
        src/main/cpp/thirdparty/freetype/include
        src/main/cpp/thirdparty/harfbuzz/src
        src/main/cpp/thirdparty/jbig2dec
        src/main/cpp/thirdparty/libjpeg
        src/main/cpp/thirdparty/lcms2/include
        src/main/cpp/thirdparty/mujs
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2
        src/main/cpp/thirdparty/zlib)

# mupdfthirdparty.a
add_library(

        # Sets the name of the library.
        mupdf-thirdparty

        # Sets the library as a shared library.
        STATIC

        # freetype
        src/main/cpp/thirdparty/freetype/src/base/ftbase.c
        src/main/cpp/thirdparty/freetype/src/base/ftbbox.c
        src/main/cpp/thirdparty/freetype/src/base/ftbitmap.c
        src/main/cpp/thirdparty/freetype/src/base/ftdebug.c
        src/main/cpp/thirdparty/freetype/src/base/ftgasp.c
        src/main/cpp/thirdparty/freetype/src/base/ftglyph.c
        src/main/cpp/thirdparty/freetype/src/base/ftinit.c
        src/main/cpp/thirdparty/freetype/src/base/ftstroke.c
        src/main/cpp/thirdparty/freetype/src/base/ftsynth.c
        src/main/cpp/thirdparty/freetype/src/base/ftsystem.c
        src/main/cpp/thirdparty/freetype/src/base/fttype1.c
        src/main/cpp/thirdparty/freetype/src/cff/cff.c
        src/main/cpp/thirdparty/freetype/src/cid/type1cid.c
        src/main/cpp/thirdparty/freetype/src/psaux/psaux.c
        src/main/cpp/thirdparty/freetype/src/pshinter/pshinter.c
        src/main/cpp/thirdparty/freetype/src/psnames/psnames.c
        src/main/cpp/thirdparty/freetype/src/raster/raster.c
        src/main/cpp/thirdparty/freetype/src/sfnt/sfnt.c
        src/main/cpp/thirdparty/freetype/src/smooth/smooth.c
        src/main/cpp/thirdparty/freetype/src/truetype/truetype.c
        src/main/cpp/thirdparty/freetype/src/type1/type1.c

        # harfbuzz
        src/main/cpp/thirdparty/harfbuzz/src/hb-aat-layout.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-blob.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-buffer-serialize.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-buffer.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-common.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-face.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-fallback-shape.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-font.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ft.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-color.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-face.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-font.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-layout.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-map.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-math.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-arabic.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-default.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-hangul.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-hebrew.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-indic-table.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-indic.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-khmer.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-myanmar.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-thai.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-tibetan.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-use-table.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-complex-use.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-fallback.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape-normalize.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-shape.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-tag.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ot-var.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-set.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-shape-plan.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-shape.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-shaper.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-static.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-ucdn.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-unicode.cc
        src/main/cpp/thirdparty/harfbuzz/src/hb-warning.cc

        # jbig2dec
        src/main/cpp/thirdparty/jbig2dec/jbig2.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_arith.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_arith_iaid.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_arith_int.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_generic.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_halftone.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_huffman.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_image.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_mmr.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_page.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_refinement.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_segment.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_symbol_dict.c
        src/main/cpp/thirdparty/jbig2dec/jbig2_text.c

        # libjpeg
        src/main/cpp/thirdparty/libjpeg/jaricom.c
        src/main/cpp/thirdparty/libjpeg/jcomapi.c
        src/main/cpp/thirdparty/libjpeg/jdapimin.c
        src/main/cpp/thirdparty/libjpeg/jdapistd.c
        src/main/cpp/thirdparty/libjpeg/jdarith.c
        src/main/cpp/thirdparty/libjpeg/jdatadst.c
        src/main/cpp/thirdparty/libjpeg/jdatasrc.c
        src/main/cpp/thirdparty/libjpeg/jdcoefct.c
        src/main/cpp/thirdparty/libjpeg/jdcolor.c
        src/main/cpp/thirdparty/libjpeg/jddctmgr.c
        src/main/cpp/thirdparty/libjpeg/jdhuff.c
        src/main/cpp/thirdparty/libjpeg/jdinput.c
        src/main/cpp/thirdparty/libjpeg/jdmainct.c
        src/main/cpp/thirdparty/libjpeg/jdmarker.c
        src/main/cpp/thirdparty/libjpeg/jdmaster.c
        src/main/cpp/thirdparty/libjpeg/jdmerge.c
        src/main/cpp/thirdparty/libjpeg/jdpostct.c
        src/main/cpp/thirdparty/libjpeg/jdsample.c
        src/main/cpp/thirdparty/libjpeg/jdtrans.c
        src/main/cpp/thirdparty/libjpeg/jerror.c
        src/main/cpp/thirdparty/libjpeg/jfdctflt.c
        src/main/cpp/thirdparty/libjpeg/jfdctfst.c
        src/main/cpp/thirdparty/libjpeg/jfdctint.c
        src/main/cpp/thirdparty/libjpeg/jidctflt.c
        src/main/cpp/thirdparty/libjpeg/jidctfst.c
        src/main/cpp/thirdparty/libjpeg/jidctint.c
        src/main/cpp/thirdparty/libjpeg/jmemmgr.c
        src/main/cpp/thirdparty/libjpeg/jquant1.c
        src/main/cpp/thirdparty/libjpeg/jquant2.c
        src/main/cpp/thirdparty/libjpeg/jutils.c

        # lcms2
        src/main/cpp/thirdparty/lcms2/src/cmsalpha.c
        src/main/cpp/thirdparty/lcms2/src/cmscam02.c
        src/main/cpp/thirdparty/lcms2/src/cmscgats.c
        src/main/cpp/thirdparty/lcms2/src/cmscnvrt.c
        src/main/cpp/thirdparty/lcms2/src/cmserr.c
        src/main/cpp/thirdparty/lcms2/src/cmsgamma.c
        src/main/cpp/thirdparty/lcms2/src/cmsgmt.c
        src/main/cpp/thirdparty/lcms2/src/cmshalf.c
        src/main/cpp/thirdparty/lcms2/src/cmsintrp.c
        src/main/cpp/thirdparty/lcms2/src/cmsio0.c
        src/main/cpp/thirdparty/lcms2/src/cmsio1.c
        src/main/cpp/thirdparty/lcms2/src/cmslut.c
        src/main/cpp/thirdparty/lcms2/src/cmsmd5.c
        src/main/cpp/thirdparty/lcms2/src/cmsmtrx.c
        src/main/cpp/thirdparty/lcms2/src/cmsnamed.c
        src/main/cpp/thirdparty/lcms2/src/cmsopt.c
        src/main/cpp/thirdparty/lcms2/src/cmspack.c
        src/main/cpp/thirdparty/lcms2/src/cmspcs.c
        src/main/cpp/thirdparty/lcms2/src/cmsplugin.c
        src/main/cpp/thirdparty/lcms2/src/cmsps2.c
        src/main/cpp/thirdparty/lcms2/src/cmssamp.c
        src/main/cpp/thirdparty/lcms2/src/cmssm.c
        src/main/cpp/thirdparty/lcms2/src/cmstypes.c
        src/main/cpp/thirdparty/lcms2/src/cmsvirt.c
        src/main/cpp/thirdparty/lcms2/src/cmswtpnt.c
        src/main/cpp/thirdparty/lcms2/src/cmsxform.c

        # mujs
        src/main/cpp/thirdparty/mujs/one.c

        # openjpeg
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/bio.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/cio.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/dwt.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/event.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/function_list.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/image.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/invert.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/j2k.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/jp2.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/mct.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/mqc.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/openjpeg.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/pi.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/sparse_array.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/t1.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/t2.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/tcd.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/tgt.c
        src/main/cpp/thirdparty/openjpeg/src/lib/openjp2/thread.c

        # zlib
        src/main/cpp/thirdparty/zlib/adler32.c
        src/main/cpp/thirdparty/zlib/compress.c
        src/main/cpp/thirdparty/zlib/crc32.c
        src/main/cpp/thirdparty/zlib/deflate.c
        src/main/cpp/thirdparty/zlib/inffast.c
        src/main/cpp/thirdparty/zlib/inflate.c
        src/main/cpp/thirdparty/zlib/inftrees.c
        src/main/cpp/thirdparty/zlib/trees.c
        src/main/cpp/thirdparty/zlib/uncompr.c
        src/main/cpp/thirdparty/zlib/zutil.c
)

################################  mupdf_thirdparty.a  ##########################################
################################################################################################


#####################################       step3          #####################################
################################################################################################
################################            mupdf.so     #######################################

# cFlags
add_definitions(-DHAVE_ANDROID)

# LOCAL_LDFLAGS
set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} -Wl,--gc-sections")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -Wl,--gc-sections")

## 设置头文件搜索路径 - 对应于Android.mk 下的 LOCAL_C_INCLUDES
include_directories(
        src/main/cpp/include/mupdf
        src/main/cpp/include/mupdf/fitz
        src/main/cpp/include/mupdf/pdf
        src/main/cpp/include/mupdf/helpers)

# mupdf.so
add_library(

        # Sets the name of the library.
        mupdf-android

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        # Associated headers in the same location as their source
        # file are automatically included.
        src/main/cpp/platform/mupdf_native.c)

find_library(

        # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library(

        # Sets the name of the path variable.
        m-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        m)

find_library(

        # Sets the name of the path variable.
        jnigraphics-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(

        # Specifies the target library.
        mupdf-android

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${m-lib}
        ${jnigraphics-lib}

        #.a
        mupdf-core
        mupdf-thirdparty)

#####################################       step3          #####################################
################################################################################################
################################            mupdf.so     #######################################