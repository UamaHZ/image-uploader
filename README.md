# Image Uploader
项目中统一的图片上传功能组件。

## 添加依赖
首先需要在项目的 `build.gradle` 文件中配置 [https://jitpack.io](https://jitpack.io) 的 maven 库：
```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```
其次在需要引用该的 module 的 `build.gradle` 中添加依赖：
```
compile 'com.github.UamaHZ:image-uploader:{version}'
```

## 用法
**首先**，在项目的 `application` 中调用初始化方法
```
LMImageUploader.init(new Config() {
    @Override
    public List<Interceptor> interceptors() {
        // 这里配置 Okhttp 的 interceptor 列表（如果需要的话）
        return null;
    }

    @Override
    public String uploadUrl() {
        // 这里配置上传图片接口路径（一定要配）
        return "http://121.40.102.80:7888/upload";
    }

    @Override
    public InputStream trustedCertificatesInputStream() {
        // 这里配置 HTTPS 证书（如果需要的话）
        return null;
    }
});
```

**然后**，在需要的地方调用上传图片方法
```
/**
 * 上传图片
 *
 * @param pathList 待上传的图片路径列表
 * @param type     类型
 * @param listener 回调接口
 */
public static void upload(List<String> pathList, String type, final UploadListener listener)
```
其中，`type` 取值为 [UploadType](https://github.com/UamaHZ/image-uploader/blob/master/src/main/java/cn/com/uama/imageuploader/UploadType.java) 中的常量，
目前只定义了 `UploadType.NEIGHBOUR` 一种用作测试。上传成功会得到上传图片在服务器上的 urls ，拿到之后作为表单的 `imageUrls` 参数的值进行传递就可以了。

**调用示例：**
```
final Dialog dialog = DialogManager.showDialog(this, "提交中，请稍后");
LMImageUploader.upload(filePathsToUpload, UploadType.NEIGHBOUR, new UploadListener() {
    @Override
    public void onSuccess(String imageUrls) {
        // 继续调用上传表单数据接口
    }

    @Override
    public void onError(String errorCode, String errorMessage) {
        DialogManager.dismiss(dialog);
        if (TextUtils.isEmpty(errorMessage)) {
            CustomToast.makeToast(mContext, "提交失败");
        } else {
            CustomToast.makeToast(mContext, errorMessage);
        }
    }
});
```

## 注意
只有图片上传成功才可以进行下一步表单数据的提交，如果图片上传失败就当做整体操作逻辑的失败，不可再调用上传表单数据接口。