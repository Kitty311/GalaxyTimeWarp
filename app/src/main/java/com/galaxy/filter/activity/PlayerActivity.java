package com.galaxy.filter.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.galaxy.filter.R;
import com.zero.hm.effect.timewarpscan.ScanActivity;

public class PlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galaxy_activity_player);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.btnTry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PlayerActivity.this, ScanActivity.class));
                finish();
            }
        });
        LinearLayout container = findViewById(R.id.container);
        boolean isImage = getIntent().getBooleanExtra("image_viewer", true);
        String path = getIntent().getStringExtra("path_uri");
        Uri uri = Uri.parse(path);
        if (isImage) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(uri);
            container.addView(imageView);
        } else {
            VideoView videoView = new VideoView(this);
            LinearLayout.LayoutParams layoutParams
                    = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            videoView.setLayoutParams(layoutParams);
            //Set MediaController  to enable play, pause, forward, etc options.
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            //Location of Media File
            //Starting VideView By Setting MediaController and URI
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.start();
            container.addView(videoView);
        }
    }
}
