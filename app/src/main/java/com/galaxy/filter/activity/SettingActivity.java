package com.galaxy.filter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.galaxy.filter.R;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galaxy_activity_setting);

        findViewById(R.id.galaxyBackButton).setOnClickListener(this);
        findViewById(R.id.galaxyShareButton).setOnClickListener(this);
        findViewById(R.id.galaxyTermButton).setOnClickListener(this);
        findViewById(R.id.galaxyPrivacyButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.galaxyBackButton) {
            finish();
        } else if (view.getId() == R.id.galaxyShareButton) {
            Intent shareIntent =   new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Insert Subject here");
            String app_url = " https://play.google.com/store/apps/details?id=com.galaxy.filter";
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,app_url);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } else if (view.getId() == R.id.galaxyTermButton) {
            startActivity(new Intent(this, TermActivity.class));
        } else if (view.getId() == R.id.galaxyPrivacyButton) {
            startActivity(new Intent(this, PrivacyActivity.class));
        }
    }
}
