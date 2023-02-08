package com.galaxy.filter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.galaxy.filter.R;
import com.google.android.material.tabs.TabLayout;

public class GalleryFragment extends Fragment {
    private Context context;
    public GalleryFragment() {

    }

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.galaxy_fragment_gallery, container, false);
        context = getContext();

        tabLayout = view.findViewById(R.id.galaxyGalleryTab);
        viewPager = view.findViewById(R.id.galaxyViewPager);
        tabLayout.addTab(tabLayout.newTab().setText("Video"));
        tabLayout.addTab(tabLayout.newTab().setText("Photo"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final GalaxyPageAdapter adapter = new GalaxyPageAdapter(tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    public class GalaxyPageAdapter extends PagerAdapter {

        private int totalTabs;
        private View pageView;

        public GalaxyPageAdapter(int totalTabs) {
            this.totalTabs = totalTabs;
        }

        // this counts total number of tabs
        @Override
        public int getCount() {
            return totalTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            pageView = position == 0 ? new GalleryVideoView(context) : new GalleryPhotoView(context);
            collection.addView(pageView);
            return pageView;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

    }
}
