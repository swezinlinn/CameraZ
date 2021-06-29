package mm.com.zin.cameraz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import mm.com.zin.cameraz.utils.Image;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static mm.com.zin.cameraz.CameraActivity.REQUEST_CODE_PERMISSIONS_STORAGE;
import static mm.com.zin.cameraz.CameraActivity.REQUIRED_PERMISSIONS_GALLERY;


public class ResultActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private Uri uri;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        uri = getIntent().getData();
        ImageView resultImage = findViewById(R.id.resultImage);
        ImageView icBack = findViewById(R.id.appBarBack);
        ImageView imgGif = findViewById(R.id.imgGif);
        ImageView shareBtn = findViewById(R.id.shareBtn);
        TextView txtTitle = findViewById(R.id.txtTitle);
        ImageView galleryBtn = findViewById(R.id.galleryBtn);

        txtTitle.setText(getResources().getString(R.string.photo_review));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue_blur));
        }

        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePhoto();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGalleryPermission();
            }
        });

        resultImage.setImageURI(uri);

        Glide.with(this).asGif()
                .apply(new RequestOptions().error(R.drawable.ic_good_day))
                .load(R.drawable.ic_good_day)
                .into(imgGif);

    }

    private void sharePhoto() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share"));
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
        Intent i = new Intent(this, PhotoGalleryActivity.class);
        startActivity(i);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void requestGalleryPermission() {
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, REQUEST_CODE_PERMISSIONS_STORAGE, REQUIRED_PERMISSIONS_GALLERY)
                .setRationale(getResources().getString(R.string.persmission_request))
                .setPositiveButtonText(R.string.ok)
                .setNegativeButtonText(R.string.cancel)
                .build());

    }

}