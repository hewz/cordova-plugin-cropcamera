#Cordova/ionic Cropped Camera for Android

This plugin is written to capture images only show in a rect.

###Install
```
cordova/ionic plugin add 插件目录
```

###Usage
```
CropCamera.takeCroppedPhoto(function (result) {
          //success 
          //image path is result.url

        }, function (err) {
          //error

        }, options); //赋值options.title可以添加标题传给Java层
```

###Uninstall
```
cordova/ionic plugin remove com.hewz.plugins.crop-camera
```

###Notice
> Running in Android 6.0 and above should firstly request Camera permission.
> This plugin uses the old Android Camera API. So it works wrong in Nexus 5X as I know.

###Todo
> Upgrade the Camera API.