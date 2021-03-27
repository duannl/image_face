package app.windbreaker.plugins.imageface.image_face;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.util.Printer;
import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.util.Log;
import android.media.ExifInterface;
import android.graphics.Matrix;

/** ImageFacePlugin */
public class ImageFacePlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "image_face");
    channel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It
  // supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new
  // Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith
  // to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith
  // will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both
  // be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "image_face");
    channel.setMethodCallHandler(new ImageFacePlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("numberOfFaces")) {
      String strpath = call.argument("image");
      result.success(numberOfFaces(strpath));
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    
  }

  public int numberOfFaces(String path)
    {
        try {
            if (path == null || path.length() == 0) {
                return 0;
            }
            File file = new File(path);
            if (!file.exists()) {
                return 0;
            }
            
            // FaceDetector only works with bitmap 565 format
            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap srcImg = BitmapFactory.decodeFile(path, bitmap_options);
            
            // FaceDetector is not working if the image's rotated
            int rotation = getCameraPhotoOrientation(path);
            if (rotation != 0) {
              Matrix matrix = new Matrix();
              matrix.setRotate(rotation);
              srcImg = Bitmap.createBitmap(srcImg, 0, 0, srcImg.getWidth(), srcImg.getHeight(), matrix, true);
            }

            int w = srcImg.getWidth();
            int h = srcImg.getHeight();

            // Improve performance for FaceDetector
            int constraintSize = 640;
            if (w > constraintSize || h > constraintSize) {
              if (w > h) {
                w = (int)(w * constraintSize / h);
                h = constraintSize;
              } else {
                h = (int)(h * constraintSize / w);
                w = constraintSize;
              }
            }
            
            // chiều rộng của ảnh phải là số chẵn
            // the image width needs to be even
            // https://stackoverflow.com/a/39136102/2721547
            if (w % 2 == 1) {
                w++;
            }
            if (h % 2 == 1) {
                h++;
            }
            
            srcImg = Bitmap.createScaledBitmap(srcImg, w, h, false);

            Log.i("[Android Plugin]","bitmap width:" + srcImg.getWidth() + " height:" + srcImg.getHeight());
            int MAX_FACE = 10;
            FaceDetector fdet_ = new FaceDetector(srcImg.getWidth(), srcImg.getHeight(), MAX_FACE);
            FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
            int faceCount = fdet_.findFaces(srcImg, fullResults);
            return faceCount;
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    public int getCameraPhotoOrientation(String imagePath){
      int rotate = 0;
      try {
          File imageFile = new File(imagePath);

          ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
          int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

          switch (orientation) {
          case ExifInterface.ORIENTATION_ROTATE_270:
              rotate = 270;
              break;
          case ExifInterface.ORIENTATION_ROTATE_180:
              rotate = 180;
              break;
          case ExifInterface.ORIENTATION_ROTATE_90:
              rotate = 90;
              break;
          }

          Log.i("RotateImage", "Exif orientation: " + orientation);
          Log.i("RotateImage", "Rotate value: " + rotate);
      } catch (Exception e) {
          e.printStackTrace();
      }
      return rotate;
  }
}
