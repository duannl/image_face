#import "ImageFacePlugin.h"

@implementation ImageFacePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"image_face"
            binaryMessenger:[registrar messenger]];
  ImageFacePlugin* instance = [[ImageFacePlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"numberOfFaces" isEqualToString:call.method]) {
      NSString *p = [call.arguments objectForKey:@"image"];
      int rst = [self numberOfFaces: p];
      result(@(rst));
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (int)numberOfFaces:(NSString*)p{
    
    NSURL * urlStr = [NSURL fileURLWithPath:p];

    CIImage *myImage = [CIImage imageWithContentsOfURL:urlStr];

    CIContext *context = [CIContext context]; 
    NSDictionary *opts = @{CIDetectorAccuracy: CIDetectorAccuracyHigh }; 
    CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeFace
                                              context:context
                                              options:opts];               


    opts = @{ CIDetectorImageOrientation :
              [[myImage properties] valueForKey:kCGImagePropertyOrientation] }; 
    NSArray *features = [detector featuresInImage:myImage options:opts];        
    return features.count;
}

@end
