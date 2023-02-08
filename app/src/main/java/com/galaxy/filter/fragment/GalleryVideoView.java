package com.galaxy.filter.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.galaxy.filter.R;
import com.galaxy.filter.helper.GalaxyItemModel;
import com.galaxy.filter.helper.GalaxyRecyclerViewAdapter;
import com.galaxy.filter.helper.GalaxyViewer;
import com.zero.hm.effect.timewarpscan.GalaxyConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryVideoView extends RelativeLayout {
    private RecyclerView recyclerView;
    private Context mContext;

    public GalleryVideoView(Context context) {
        super(context);
        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.galaxy_page_video, null);
        recyclerView = view.findViewById(R.id.galaxyRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        if (recyclerView == null) return;
        List<GalaxyItemModel> list = new ArrayList<>();
        File appDirectory = new File(GalaxyConstants.FILTER_IMAGE_SAVED_PATH);
        if (appDirectory.exists()) {
            File[] filePaths = appDirectory.listFiles();
            if (filePaths != null)
                for (File s : filePaths) {
                    if (!s.getName().contains(".mp4") || s.isDirectory()) continue;
                    GalaxyItemModel itemModel = new GalaxyItemModel();
                    itemModel.uriPath = s.toURI().toString();
                    itemModel.path = s;
                    itemModel.isImage = s.getName().contains(".jpg");
                    list.add(itemModel);
                }
        }
        GalaxyRecyclerViewAdapter grvh = new GalaxyRecyclerViewAdapter(list, mContext, false);
//        GalaxyRecyclerViewAdapter.setOnItemClickListener(new GalaxyRecyclerViewAdapter.ClickListener() {
//            @Override
//            public void onItemClick(int position, View v) {
//                GalaxyItemModel itemModel = list.get(position);
//                Intent intent = new Intent(mContext, GalaxyViewer.class);
//                intent.putExtra("image_viewer", false);
//                intent.putExtra("path_uri", itemModel.uriPath);
//                mContext.startActivity(intent);
//            }
//            @Override
//            public void onItemLongClick(int position, View v) {
//            }
//        });
        recyclerView.setAdapter(grvh);
        addView(view);
    }

}
