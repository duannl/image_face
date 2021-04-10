package app.windbreaker.plugins.imageface.image_face;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.MethodChannel.Result;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.media.ExifInterface;
import android.graphics.Matrix;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
        String path = call.argument("image");
        numberOfFaces(path, result);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    
  }

  public void numberOfFaces(String path, Result rawResult)
    {
        Result result = new MethodResultWrapper(rawResult);
        try {
            if (path == null || path.length() == 0) {
                result.success(0);
                return;
            }
            File file = new File(path);
            if (!file.exists()) {
                result.success(0);
                return;
            }
            // FaceDetector is not working if the image's rotated
            Bitmap srcImg = rotateBitmap(path);

            int w = srcImg.getWidth();
            int h = srcImg.getHeight();

            // Improve performance for FaceDetector
            // https://firebase.google.com/docs/ml-kit/android/detect-faces
            // For ML Kit to accurately detect faces, input images must contain faces that are represented by sufficient pixel data.
            // In general, each face you want to detect in an image should be at least 100x100 pixels.
            // If you want to detect the contours of faces,
            // ML Kit requires higher resolution input: each face should be at least 200x200 pixels.
            // => we need to detect at least two faces, so size is at least 400x400 pixels
            double constraintSize = 600.0;
            double scale = calcScale((double)w, (double)h, constraintSize, constraintSize);
            w = (int)((double)w / scale);
            h = (int)((double)h / scale);

            srcImg = Bitmap.createScaledBitmap(srcImg, w, h, false);

            Log.i("[Android Plugin]","bitmap width:" + srcImg.getWidth() + " height:" + srcImg.getHeight());
            // rotation degree = 0 because we already rotate srcImage
            new MLKitFace().runFaceContourDetection(srcImg, 0, result);
        }
        catch (Exception e)
        {
            result.success(0);
        }
    }

    /* https://pub.dev/packages/flutter_image_compress
    minWidth and minHeight are constraints on image scaling
    For example, a 4000*2000 image, minWidth set to 1920, minHeight set to 1080
    var scale = calcScale(srcWidth: 4000, srcHeight: 2000, minWidth: 1920, minHeight: 1080);
    print("scale = $scale"); // scale = 1.8518518518518519
    print("target width = ${4000 / scale}, height = ${2000 / scale}"); // target width = 2160.0, height = 1080.0 */

    double calcScale(
        double srcWidth,
        double srcHeight,
        double minWidth,
        double minHeight
    ) {
        double scaleW = srcWidth / minWidth;
        double scaleH = srcHeight / minHeight;

        return max(1.0, min(scaleW, scaleH));
    }

    // https://stackoverflow.com/a/20480741
    public static Bitmap rotateBitmap(String imagePath) {
        ExifInterface exif = null;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("[Android Plugin]","Fail to read exif of photo");
            return bitmap;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Log.i("[Android Plugin]", "Exif orientation: " + orientation);

        Matrix matrix = new Matrix();
        switch (orientation) {

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            Log.i("RotateImage", "Out of memory");
            return bitmap;
        }
    }
}
