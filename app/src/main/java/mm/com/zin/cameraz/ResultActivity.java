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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


public class ResultActivity extends AppCompatActivity {
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
}