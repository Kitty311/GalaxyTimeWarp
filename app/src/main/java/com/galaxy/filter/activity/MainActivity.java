package com.galaxy.filter.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentTransaction;

import com.galaxy.filter.R;
import com.galaxy.filter.fragment.ExploreFragment;
import com.galaxy.filter.fragment.GalleryFragment;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.zero.hm.effect.timewarpscan.ScanActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galaxy_activity_main);

        BottomAppBar bottomAppBar = findViewById(R.id.galaxyBottomBar);
        MaterialShapeDrawable bottomBarBackground = (MaterialShapeDrawable) bottomAppBar.getBackground();
        bottomBarBackground.setShapeAppearanceModel(
                bottomBarBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setAllCorners(new RoundedCornerTreatment()).setAllCornerSizes(new RelativeCornerSize(0.2f))
                        .build());

        exploreButton = findViewById(R.id.galaxyExploreButton);
        exploreButton.setOnClickListener(this);
        galleryButton = findViewById(R.id.galaxyGalleryButton);
        galleryButton.setOnClickListener(this);
        titleText = findViewById(R.id.galaxyTitleText);
        findViewById(R.id.galaxyCameraButton).setOnClickListener(this);
        findViewById(R.id.galaxySettingButton).setOnClickListener(this);
        switchTab(true);

//        ArrayList<String> list = new ArrayList<>();
//        for (int i = 0; i < permissions.length; i++) {
//            if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
//                list.add(permissions[i]);
//            }
//        }
//        if (list.size() != 0) {
//            String[] s = new String[list.size()];
//            for (int i = 0; i < list.size(); i++) {
//                s[i] = list.get(i);
//            }
//            ActivityCompat.requestPermissions(this, s, 5);
//        }

    }

//    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private MaterialButton exploreButton, galleryButton;
    private TextView titleText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.galaxy_bottom_nav_menu, menu);
        return true;
    }

    public void switchTab(boolean isExplore) {
        exploreButton.setSelected(isExplore);
        galleryButton.setSelected(!isExplore);
        titleText.setText(isExplore ? "Explore" : "Gallery");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.galaxyContainer, isExplore ? new ExploreFragment() : new GalleryFragment());
        ft.commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.galaxyCameraButton) {
            startActivity(new Intent(this, ScanActivity.class));
        } else if (v.getId() == R.id.galaxySettingButton) {
            startActivity(new Intent(this, SettingActivity.class));
        } else {
            switchTab(v.getId() == R.id.galaxyExploreButton);
        }
    }
}