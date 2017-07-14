# Cordova/ionic Crop Camera for Android

拍摄指定矩形框内的图片

### 安装
```
cordova/ionic plugin add 插件目录
```

### 使用
```
CropCamera.takeCroppedPhoto(function (result) {
          //success 
          //image path is result.url

        }, function (err) {
          //error

        }, options); //赋值options.title可以设置相机界面标题
```

### 卸载
```
cordova/ionic plugin remove hewz.plugins.crop_camera
```

### Notice
> * Running in Android 6.0 and above should firstly request Camera permission.

> * This plugin uses the old Android Camera API. So it works wrong in Nexus 5X as I know.

### Todo
> Upgrade the Camera API.
