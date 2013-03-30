/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.janrenz.app.ardtheke;

import java.util.ArrayList;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import de.janrenz.app.ardtheke.R;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays a particular news article onscreen.
 *
 * This activity is started only when the screen is not large enough for a two-pane layout, in
 * which case this separate activity is shown in order to display the news article. This activity
 * kills itself if the display is reconfigured into a shape that allows a two-pane layout, since
 * in that case the news article will be displayed by the {@link NewsReaderActivity} and this
 * Activity therefore becomes unnecessary.
 */
public class ArticleActivity extends FragmentActivity  implements ArticleFragment.OnMovieClickedListener {
    // The news category index and the article index for the article we are to display
    int mCatIndex, mArtIndex;
    //the external id
    String extId;
    ArrayList<String> allIds;
    MyAdapter mAdapter;

    ViewPager mPager;
    /**
     * Sets up the activity.
     *
     * Setting up the activity means reading the category/article index from the Intent that
     * fired this Activity and loading it onto the UI. We also detect if there has been a
     * screen configuration change (in particular, a rotation) that makes this activity
     * unnecessary, in which case we do the honorable thing and get out of the way.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCatIndex = getIntent().getExtras().getInt("catIndex", 0);
        mArtIndex = getIntent().getExtras().getInt("artIndex", 0);
        extId     = getIntent().getExtras().getString("extId");
        allIds  = getIntent().getExtras().getStringArrayList("allIds");
        
        // If we are in two-pane layout mode, this activity is no longer necessary
        if (getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
            return;
        }
        setContentView( R.layout.daylistwrapper );
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mAdapter.setCount(allIds.size());
        mAdapter.setAllItems(allIds);
        mPager = (ViewPager)findViewById(R.id.pager);
       
        mPager.setAdapter(mAdapter);
      //Set the pager with an adapter
    

        //Bind the title indicator to the adapter
        LinePageIndicator titleIndicator = (LinePageIndicator)findViewById(R.id.pageindicator);
        titleIndicator.setViewPager(mPager);
        mPager.setCurrentItem(mArtIndex);
     
    }
    
	
    public static class MyAdapter extends FragmentPagerAdapter {
    	private int mcount = 0;
    	private ArrayList<String> mallIds = null;
    	//TODO keep an instance of the fragment per id
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }
        public void setCount( int newCount ) {
        	mcount = newCount;
        }
        public void setAllItems(ArrayList<String> allIds) {
        	 mallIds = allIds;
        }
        public String getPageTitle (int position ) {
        	return "Titel" + position;
        }
        
        @Override
        public int getCount() {
        	return mcount;
           
        }

        @Override
        public Fragment getItem(int position) {
        	ArticleFragment f = new ArticleFragment();
        Bundle args = new Bundle();
        	args.putInt("num", position);
            args.putString("extId", mallIds.get(position));
            f.setArguments(args);
            return f;
        }
    }
    
    /**
     * Not used anymore, we treat this oin the fragment
     */
	@Override
	public void onMovieSelected(String url) {
		// TODO Auto-generated method stub
		Log.v("THIS", url);
		
	}
}
