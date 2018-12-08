##
## 预览图片
![image](https://github.com/153437803/MuPDF/blob/master/Screenrecorder-2018-09-22.gif )

##
## 本库简介
```
WPS文件预览, 基于TBS编译, 可以打开本地文件, 网络文件
PDF文件预览, 基于MuPDF1.1.3编译, 可以打开本地文件, 网络文件
打开网络文件需要okhttp获取网络文件的byte[]或者InputStream
```

##
## 打开RAW文件夹[本地文件]
```
MupdfUtil.openFromRaw(final Context context, final int id);
```

##
## 打开Asset文件夹[本地文件]
```
MupdfUtil.openFromAsset(final Context context, final String name);
```

##
## 打开sd卡文件[本地文件]
```
3.打开sd卡文件
MupdfUtil.openFromLocal(final Context context, final String name);
```

##
## 打开byte[]文件[网络文件]
```
MupdfUtil.openFromByte(final Context context, final byte[] bytes);
```

##
## 打开InputStream文件[网络文件]
```
MupdfUtil.openFromInputStream(final Context context, final InputStream stm);
```

##
## TODO
```
编辑精简MuPDF1.1.3
```
