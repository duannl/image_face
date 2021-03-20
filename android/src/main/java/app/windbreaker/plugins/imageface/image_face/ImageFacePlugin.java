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
            // chiều rộng của ảnh phải là số chẵn
            // the image width needs to be even
            // https://stackoverflow.com/a/39136102/2721547
            Bitmap srcImg = BitmapFactory.decodeFile(path);
            Bitmap srcFace = srcImg.copy(Bitmap.Config.RGB_565, true);
            srcImg = null;
            int w = srcFace.getWidth();
            int h = srcFace.getHeight();
            if (w % 2 == 1) {
                w++;
                srcFace = Bitmap.createScaledBitmap(srcFace,
                        srcFace.getWidth()+1, srcFace.getHeight(), false);
            }
            if (h % 2 == 1) {
                h++;
                srcFace = Bitmap.createScaledBitmap(srcFace,
                        srcFace.getWidth(), srcFace.getHeight()+1, false);
            } 

            Log.e("[Android Plugin]","bitmap width:" + srcFace.getWidth() + " height:" + srcFace.getHeight());
            int MAX_FACE = 10;
            FaceDetector fdet_ = new FaceDetector(srcFace.getWidth(), srcFace.getHeight(), MAX_FACE);
            FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
            fdet_.findFaces(srcFace, fullResults);

            int counter = 0;
            for (int i = 0; i < MAX_FACE; i++) {
              if (fullResults[i] != null) {
                  counter = counter + 1;
              }
            }
            return counter;
        }
        catch (Exception e)
        {
            return 0;
        }
    }
}
