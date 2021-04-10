package app.windbreaker.plugins.imageface.image_face;

import android.graphics.Bitmap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import io.flutter.plugin.common.MethodChannel.Result;

// https://developers.google.com/ml-kit/vision/face-detection
public class MLKitFace {
    void runFaceContourDetection(Bitmap mSelectedImage, int rotationDegree, final Result result) {
        final InputImage image = InputImage.fromBitmap(mSelectedImage, rotationDegree);
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                Log.i("[Android Plugin]","MLKit success with number of faces: " + faces.size());
                                result.success(faces.size());
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Log.i("[Android Plugin]","MLKit failure");
                                e.printStackTrace();
                                result.success(0);
                            }
                        });

    }
}

// MethodChannel.Result wrapper that responds on the platform thread.
class MethodResultWrapper implements Result {
    private Result methodResult;
    private Handler handler;

    MethodResultWrapper(Result result) {
        methodResult = result;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                methodResult.success(result);
            }
        });
    }

    @Override
    public void error(final String errorCode, final String errorMessage, final Object errorDetails) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                methodResult.error(errorCode, errorMessage, errorDetails);
            }
        });
    }

    @Override
    public void notImplemented() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                methodResult.notImplemented();
            }
        });
    }
}