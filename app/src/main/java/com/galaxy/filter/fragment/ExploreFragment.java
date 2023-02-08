package com.galaxy.filter.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.galaxy.filter.helper.GalaxyItemModel;
import com.galaxy.filter.helper.GalaxyRecyclerViewAdapter;
import com.galaxy.filter.R;
import com.galaxy.filter.helper.GalaxyViewer;
import com.zero.hm.effect.timewarpscan.GalaxyConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {
    public ExploreFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.galaxy_fragment_explore, container, false);
        Context context = getContext();
        RecyclerView recyclerView = view.findViewById(R.id.galaxyRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        List<GalaxyItemModel> list = new ArrayList<>();
//        if (context == null) return view;
//        File exploreFolder = new File(GalaxyConstants.GetExploreVideoSavedPath(context));
//        if (!exploreFolder.exists()) return view;
        String[] fileList = new String[] {
                "explore_video1.jpg",
                "explore_video2.jpg",
                "explore_video3.jpg",
                "explore_video4.jpg",
                "explore_video5.jpg",
                "explore_video6.jpg",
        };
        int[] videoList = new int[] {
                R.raw.explore_video1,
                R.raw.explore_video2,
                R.raw.explore_video3,
                R.raw.explore_video4,
                R.raw.explore_video5,
                R.raw.explore_video6,
        };
        if (fileList != null) {
            int i = 0;
            for (String s : fileList) {
                if (!s.contains(".jpg")) continue;
                GalaxyItemModel itemModel = new GalaxyItemModel();
                itemModel.uriPath = "android.resource://" + context.getPackageName() + "/" + videoList[i ++];
                itemModel.path = new File(GalaxyConstants.APP_FOLDER + "/" + s);
                itemModel.isImage = s.contains(".jpg");
                list.add(itemModel);
            }
        }
        GalaxyRecyclerViewAdapter grvh = new GalaxyRecyclerViewAdapter(list, context, true);
        GalaxyRecyclerViewAdapter.setOnItemClickListener(new GalaxyRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                GalaxyItemModel itemModel = list.get(position);
                Intent intent = new Intent(context, GalaxyViewer.class);
                intent.putExtra("image_viewer", false);
                intent.putExtra("path_uri", itemModel.uriPath);
                startActivity(intent);
            }
            @Override
            public void onItemLongClick(int position, View v) {
            }
        });
        recyclerView.setAdapter(grvh);
        return view;
    }
}
