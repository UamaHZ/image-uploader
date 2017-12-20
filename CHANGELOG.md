# Change Log

## Version 1.2.0 *(2017-12-20)*
* 废弃之前的初始化方法，新增 `LMImageUploader#init(Config config)` ，内部不再提供默认上传路径，必须从外部设置
* 设置接口超时时间（包括连接/读取/写入）为 60 秒

## Version 1.1.0 *(2017-11-29)*
`LMImageUploader` 新增压缩后再上传方法: [LMImageUploader#compressAndUpload](https://github.com/UamaHZ/image-uploader/blob/4d26d9ca6defb50da63c54d084290651e7749c19/library/src/main/java/cn/com/uama/imageuploader/LMImageUploader.java#L185) 。

## Version 1.0.0 *(2017-08-12)*
[UploadType](https://github.com/UamaHZ/image-uploader/blob/master/src/main/java/cn/com/uama/imageuploader/UploadType.java) 增加 `COMMUNITY`,`USER` 两种类型。

## Version 0.0.3 *(2017-08-01)*
[UploadType](https://github.com/UamaHZ/image-uploader/blob/master/src/main/java/cn/com/uama/imageuploader/UploadType.java) 增加 `HEAD`,`ORDER`,`DECORATION`,`DEVICE` 几种类型。

## Version 0.0.2 *(2017-07-10)*
* LMImageUploader 增加 uploadFiles 方法。
* 初始化方法变为 `init(Config config, boolean debug)` ，并增加 `init(boolean debug)` 方法。如果没有配置 uploadUrl ，会使用默认接口，
这样在保持灵活性的同时简化配置。
* 增加 `Config` 接口的一个简单实现类 `SimpleConfig` ，从而只需覆写需要覆写的方法。

## Version 0.0.1 *(2017-07-04)*
Initial release.
