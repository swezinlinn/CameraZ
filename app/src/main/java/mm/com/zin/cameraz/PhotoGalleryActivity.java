package mm.com.zin.cameraz;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Map;

import mm.com.zin.cameraz.adapter.FoldersAdapter;
import mm.com.zin.cameraz.utils.Image;
import mm.com.zin.cameraz.utils.StorageHelper;

public class PhotoGalleryActivity extends AppCompatActivity {

    private RecyclerView galleryFoldersRCV;
    private ProgressBar progressBar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.photo_gallery);
        ImageView icBack = findViewById(R.id.appBarBack);
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        galleryFoldersRCV = findViewById(R.id.galleryRCV);
        progressBar = findViewById(R.id.progressBar);

        galleryFoldersRCV.setHasFixedSize(true);
        galleryFoldersRCV.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        loadFolders();
    }

    private void loadFolders() {
        StorageHelper storageHelper = new StorageHelper(getApplicationContext());
        Map<String, ArrayList<Image>> folders = storageHelper.getGalleryFolders();

        FoldersAdapter adapter = new FoldersAdapter(getApplicationContext(), folders);
        galleryFoldersRCV.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }
}