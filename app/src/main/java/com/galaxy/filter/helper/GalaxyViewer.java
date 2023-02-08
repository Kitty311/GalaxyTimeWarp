package com.galaxy.filter.helper;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class GalaxyViewer extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout container = new LinearLayout(this);
        container.setGravity(Gravity.CENTER);
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
        setContentView(container);
    }
}
