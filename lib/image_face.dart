import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

/// ImageFace class the namespace of this lib
class ImageFace {
  /// method channel for image face
  static const MethodChannel _channel = const MethodChannel('image_face');

  /// return the number of faces in the image
  static Future<int> numberOfFaces(File image) async {
    if (image == null) {
      // if the file is null it is no face
      return 0;
    }

    /// image path
    final Map<String, String> arg = {'image': image.path};

    /// incoke native method
    final int number = await _channel.invokeMethod('numberOfFaces', arg);

    // return result
    return number;
  }
}
