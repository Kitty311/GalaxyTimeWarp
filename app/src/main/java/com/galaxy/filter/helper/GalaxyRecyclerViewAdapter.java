package com.galaxy.filter.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.galaxy.filter.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GalaxyRecyclerViewAdapter extends RecyclerView.Adapter<GalaxyRecyclerViewAdapter.GalaxyViewHolder> {

    private static ClickListener clickListener;
    private final List<GalaxyItemModel> mList;
    private final Context context;
    private final boolean isForExplore;

    public GalaxyRecyclerViewAdapter(List<GalaxyItemModel> list, Context mContext, boolean mIsForExplore) {
        this.mList = list;
        context = mContext;
        isForExplore = mIsForExplore;
    }

    @NonNull
    @Override
    public GalaxyRecyclerViewAdapter.GalaxyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GalaxyViewHolder(LayoutInflater.from(context).inflate(R.layout.galaxy_item_timewarp, null));
    }

    @Override
    public void onBindViewHolder(@NonNull GalaxyViewHolder holder, int position) {
        try {
            GalaxyItemModel itemModel = mList.get(position);
            holder.playIcon.setVisibility(itemModel.isImage && !isForExplore ? View.INVISIBLE : View.VISIBLE);
            if (itemModel.isImage) {
                InputStream inputStream;
                if (isForExplore) {
                    inputStream = context.getAssets()
                            .open(itemModel.path.getPath());
                } else {
                    inputStream = new FileInputStream(itemModel.path);
                }
                holder.thumbnail.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                holder.thumbnail.setImageBitmap(ThumbnailUtils.createVideoThumbnail(itemModel.path.getAbsolutePath(),
                        MediaStore.Video.Thumbnails.MINI_KIND));
            }

            if (!isForExplore) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, GalaxyViewer.class);
                        intent.putExtra("image_viewer", itemModel.isImage);
                        intent.putExtra("path_uri", itemModel.uriPath);
                        context.startActivity(intent);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static void setOnItemClickListener(ClickListener clickListener) {
        GalaxyRecyclerViewAdapter.clickListener = clickListener;
    }

    public static class GalaxyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private final ImageView thumbnail;
        private final View playIcon;

        public GalaxyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            thumbnail = itemView.findViewById(R.id.galaxyThumbnail);
            playIcon = itemView.findViewById(R.id.galaxyPlayIcon);
        }
        @Override
        public void onClick(View v) {
            if (clickListener == null) return;
            clickListener.onItemClick(getAdapterPosition(), v);
        }
        @Override
        public boolean onLongClick(View v) {
            if (clickListener == null) return false;
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

}
