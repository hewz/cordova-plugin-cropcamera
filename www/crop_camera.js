var exec = require('cordova/exec');

var CropCamera = {
  takeCroppedPhoto: function (successFn, failureFn, options) {
    if (options)
      exec(successFn, failureFn, 'CropCamera', 'takeCroppedPhoto', [options.title]);
    else
      exec(successFn, failureFn, 'CropCamera', 'takeCroppedPhoto', []);
  }
};

module.exports = CropCamera;

