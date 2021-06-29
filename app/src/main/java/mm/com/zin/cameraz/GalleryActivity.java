package mm.com.zin.cameraz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import mm.com.zin.cameraz.adapter.GalleryAdapter;
import mm.com.zin.cameraz.utils.Image;


public class GalleryActivity extends AppCompatActivity {

    private ArrayList<Image> selectedImages = new ArrayList<>();
    int EDITED_IMAGE_RESULT_CODE= 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();
        ArrayList<Image> images = intent.getParcelableArrayListExtra("images");
        String folderName = intent.getStringExtra("folder_name");

        RecyclerView galleryRcv = findViewById(R.id.galleryRCV);
        galleryRcv.setHasFixedSize(true);
        galleryRcv.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));

        GalleryAdapter galleryAdapter = new GalleryAdapter(getApplicationContext(), images);
        galleryRcv.setAdapter(galleryAdapter);

        TextView headerText = findViewById(R.id.txtTitle);
        headerText.setText(folderName);

        ImageView icBack = findViewById(R.id.appBarBack);
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        galleryAdapter.setOnItemClickListener(image -> {
            if(image.isChecked()) {
                Uri photoURI = Uri.parse("file://"+image.getImgUri());

                // navigating to edit activity after capturing image from camera
                Intent dsPhotoEditorIntent = new Intent(GalleryActivity.this, DsPhotoEditorActivity.class);
                dsPhotoEditorIntent.setData(photoURI);

                // directory for edited images
                dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "CameraZ");

                int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
                dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
                startActivityForResult(dsPhotoEditorIntent, EDITED_IMAGE_RESULT_CODE);
            }
        });

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
            }
        }
    }

}