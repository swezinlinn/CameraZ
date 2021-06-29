package mm.com.zin.cameraz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import mm.com.zin.cameraz.utils.Analyzer;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class CameraActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private final int REQUEST_CODE_PERMISSIONS_CAMERA = 10;
    public static final int REQUEST_CODE_PERMISSIONS_STORAGE = 100;

    private static final String TAG = "CameraZApp";
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    int EDITED_IMAGE_RESULT_CODE= 200;

    private final String[] REQUIRED_PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    public static final String[] REQUIRED_PERMISSIONS_GALLERY = {Manifest.permission.READ_EXTERNAL_STORAGE,};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        viewFinder = findViewById(R.id.view_finder);
        // Request camera permissions
       requestCameraPermission();

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                    int oldRight, int oldBottom) {
                updateTransform();
            }
        });

        ImageView imgGallery  = findViewById(R.id.photo_view_button);
        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGalleryPermission();
            }
        });
    }

    private TextureView viewFinder;

    private final Runnable startCamera = new Runnable() {
        @Override
        public void run() {
            CameraX.unbindAll();

            // Create configuration object for the viewfinder use case
            PreviewConfig previewConfig = new PreviewConfig.Builder()
                    .setTargetAspectRatio(new Rational(1, 1))
                    .setTargetResolution(new Size(640, 640))
                    .setLensFacing(lensFacing)
                    .build();

            // Build the viewfinder use case
            Preview preview = new Preview(previewConfig);

            // Every time the viewfinder is updated, recompute layout
            preview.setOnPreviewOutputUpdateListener(
                    previewOutput -> {
                        // To update the SurfaceTexture, we have to remove it and re-add it
                        ViewGroup parent = (ViewGroup) viewFinder.getParent();
                        parent.removeView(viewFinder);
                        parent.addView(viewFinder, 0);

                        viewFinder.setSurfaceTexture(previewOutput.getSurfaceTexture());
                        updateTransform();
                    });

            // Create configuration object for the image capture use case
            ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                    .setTargetAspectRatio(new Rational(1, 1))
                    // We don't set a resolution for image capture; instead, we
                    // select a capture mode which will infer the appropriate
                    // resolution based on aspect ration and requested mode
                    .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                    .setLensFacing(lensFacing)
                    .build();

            // Build the image capture use case and attach button click listener
            ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);

            findViewById(R.id.capture_button).setOnClickListener(view -> {
                File file = new File(getExternalMediaDirs()[0], System.currentTimeMillis() + ".jpg");
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener(){
                    @Override
                    public void onError(ImageCapture.UseCaseError error, String message,
                                        @Nullable Throwable exc) {
                        String msg = "Photo capture failed: " + message;
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, msg);
                        if (exc != null) {
                            exc.printStackTrace();
                        }
                    }

                    @Override
                    public void onImageSaved(File file) {
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                BuildConfig.APPLICATION_ID+".provider",
                                file);
                        Log.d("URI","image uri-camera--."+photoURI);

                        // navigating to edit activity after capturing image from camera
                        Intent dsPhotoEditorIntent = new Intent(CameraActivity.this, DsPhotoEditorActivity.class);
                        dsPhotoEditorIntent.setData(photoURI);

                        // directory for edited images
                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "CameraZ");

                        int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
                        startActivityForResult(dsPhotoEditorIntent, EDITED_IMAGE_RESULT_CODE);

                    }
                });

            });

            // Setup image analysis pipeline that computes average pixel luminance
            HandlerThread analyzerThread = new HandlerThread("LuminosityAnalysis");
            analyzerThread.start();
            ImageAnalysisConfig analyzerConfig =
                    new ImageAnalysisConfig.Builder()
                            .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                            // In our analysis, we care more about the latest image than
                            // analyzing *every* image
                            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                            .setLensFacing(lensFacing)
                            .build();

            ImageAnalysis analyzerUseCase = new ImageAnalysis(analyzerConfig);
            analyzerUseCase.setAnalyzer(new Analyzer());

            // Bind use cases to lifecycle
            CameraX.bindToLifecycle((LifecycleOwner) CameraActivity.this, preview, imageCapture,
                    analyzerUseCase);
        }
    };


    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private void updateCameraSwitchButton() {
        ImageButton switchButton = findViewById(R.id.camera_switch_button);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (CameraX.LensFacing.FRONT == lensFacing) {
                    lensFacing = CameraX.LensFacing.BACK;
                } else {
                    lensFacing = CameraX.LensFacing.FRONT;
                }
                try {
                    viewFinder.post(startCamera);
                    // Only bind use cases if we can query a camera with this orientation
                    CameraX.getCameraWithLensFacing(lensFacing);
                } catch (Exception exc) {
                    // Do nothing
                }
            }
        });
        switchButton.setEnabled(true);
    }

    public Uri getImageUri(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String path= MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", "Desc");
        return Uri.parse(path);
    }

    private void setGalleryThumbnail(Uri uri) {
        // Reference of the view that holds the gallery thumbnail
        ImageButton thumbnail = findViewById(R.id.photo_view_button);
        thumbnail.setPadding(4,4,4,4);
        // Remove thumbnail padding

        // Load thumbnail into circular button using Glide
        Glide.with(thumbnail)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail);

    }

    private void updateTransform() {
        Matrix matrix = new Matrix();

        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;

        // Correct preview output to account for display rotation
        float rotationDegrees;
        switch (viewFinder.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0f;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90f;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180f;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270f;
                break;
            default:
                return;
        }

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDITED_IMAGE_RESULT_CODE){
            if(data.getData()!=null) {
                Toast.makeText(this, "Image saved to gallery.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ResultActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Update the gallery thumbnail with latest picture taken
                    setGalleryThumbnail(data.getData());
                }
            }
        }
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(requestCode == REQUEST_CODE_PERMISSIONS_CAMERA) {

            viewFinder.post(startCamera);
            updateCameraSwitchButton();
        }else {
            Intent i = new Intent(this, PhotoGalleryActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void requestCameraPermission() {
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, REQUEST_CODE_PERMISSIONS_CAMERA, REQUIRED_PERMISSIONS_CAMERA)
                .setRationale(getResources().getString(R.string.persmission_request))
                .setPositiveButtonText(R.string.ok)
                .setNegativeButtonText(R.string.cancel)
                .build());

    }

    private void requestGalleryPermission() {
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, REQUEST_CODE_PERMISSIONS_STORAGE, REQUIRED_PERMISSIONS_GALLERY)
                .setRationale(getResources().getString(R.string.persmission_request))
                .setPositiveButtonText(R.string.ok)
                .setNegativeButtonText(R.string.cancel)
                .build());

    }
}
