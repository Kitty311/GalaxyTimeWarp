package com.zero.hm.effect.timewarpscan;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        path = getIntent().getStringExtra("file_path");
        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnRetry).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        previewImage = findViewById(R.id.imageView);
        previewVideo = findViewById(R.id.videoView);
        if (path == null || path.isEmpty()) return;
        if (path.contains(".jpg")) {
            previewImage.setVisibility(View.VISIBLE);
            try {
                InputStream inputStream = new FileInputStream(path);
                previewImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            previewVideo.setVisibility(View.VISIBLE);
            //Set MediaController  to enable play, pause, forward, etc options.
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(previewVideo);
            //Location of Media File
            //Starting VideView By Setting MediaController and URI
            previewVideo.setMediaController(mediaController);
            previewVideo.setVideoURI(Uri.parse(path));
            previewVideo.requestFocus();
            previewVideo.start();
        }

    }

    private ImageView previewImage;
    private VideoView previewVideo;
    private String path;

    private boolean deleteSavedFile() {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        if (file.exists()) return file.delete();
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnRetry) {
            Log.e("PreviewActivity", deleteSavedFile() ? "File Successfully deleted" : "Failed to delete file");
            startActivity(new Intent(this, ScanActivity.class));
            finish();
        } else if (view.getId() == R.id.btnSave) {
            Toast.makeText(this, "Saved file path: " + path, Toast.LENGTH_LONG).show();
            finish();
        } else if (view.getId() == R.id.btnBack) {
            Log.e("PreviewActivity", deleteSavedFile() ? "File Successfully deleted" : "Failed to delete file");
            finish();
        }
    }
}
