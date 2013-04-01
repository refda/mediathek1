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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import de.janrenz.app.ardtheke.R;
import de.janrenz.app.ardtheke.ArticleActivity.MyAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;

/**
 * Main activity: shows headlines list and articles, if layout permits.
 *
 * This is the main activity of the application. It can have several different layouts depending
 * on the SDK version, screen size and orientation. The configurations are divided in two large
 * groups: single-pane layouts and dual-pane layouts.
 *
 * In single-pane mode, this activity shows a list of headlines using a {@link HeadlinesFragment}.
 * When the user clicks on a headline, a separate activity (a {@link ArticleActivity}) is launched
 * to show the news article.
 *
 * In dual-pane mode, this activity shows a {@HeadlinesFragment} on the left side and an
 * {@ArticleFragment} on the right side. When the user selects a headline on the left, the
 * corresponding article is shown on the right.
 *
 * If an Action Bar is available (large enough screen and SDK version 11 or up), navigation
 * controls are shown in the Action Bar (whether to show tabs or a list depends on the layout).
 * If an Action Bar is not available, a regular image and button are shown in the top area of
 * the screen, emulating an Action Bar.
 * 
 *CompatActionBarNavListener,OnClickListener 
 */
public class NewsReaderActivity extends FragmentActivity
        implements HeadlinesFragment.OnHeadlineSelectedListener
                    {

    // Whether or not we are in dual-pane mode
    boolean mIsDualPane = false;

    // The fragment where the headlines are displayed
    HeadlinesFragment mHeadlinesFragment;

    // The fragment where the article is displayed (null if absent)
    ArticleFragment mArticleFragment;

    // The news category and article index currently being displayed
    int mCatIndex = 0;
    int mArtIndex = 0;
    NewsCategory mCurrentCat;

    // List of category titles
  
    MyAdapter mAdapter;
    ViewPager mPager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // find our fragments
        mHeadlinesFragment = (HeadlinesFragment) getSupportFragmentManager().findFragmentById(
                R.id.pager);
        mArticleFragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(
                R.id.article);

        // Determine whether we are in single-pane or dual-pane mode by testing the visibility
        // of the article view.
        View articleView = findViewById(R.id.article);
        mIsDualPane = articleView != null && articleView.getVisibility() == View.VISIBLE;

        // Register ourselves as the listener for the headlines fragment events.
       //

        // Set up the Action Bar (or not, if one is not available)
        int catIndex = savedInstanceState == null ? 0 : savedInstanceState.getInt("catIndex", 0);
        //setUpActionBar(mIsDualPane, catIndex);

        // Set up headlines fragment
        //mHeadlinesFragment.setSelectable(mIsDualPane);
        restoreSelection(savedInstanceState);

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        //Bind the title indicator to the adapter
        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.pageindicator);
        titleIndicator.setViewPager(mPager);
        mPager.setCurrentItem(6);
      
        
    }

    public static class MyAdapter extends FragmentPagerAdapter {
    	private int mcount = 7;
    	
    	//TODO keep an instance of the fragment per id
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }
        public void setCount( int newCount ) {
        	mcount = newCount;
        }
       
        public String getPageTitle (int position ) {
        	//!TODO: get real page title
            Date dt = new Date();
            // z.B. 'Fri Jan 26 19:03:56 GMT+01:00 2001'
          dt.setHours(20);
          dt.setMinutes(0);
          dt.setSeconds(0);
          if (position == (mcount-1) ) {
        	  return "Heute";
          }
          
          Long fragmentTime = dt.getTime() - ((24*60*60*1000)*(mcount-position-1));
          Date cdt = new Date(fragmentTime);
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE dd.MM");
          return simpleDateFormat.format(fragmentTime);
        	
        }
        
        @Override
        public int getCount() {
        	return mcount;  
        }

        @Override
        public Fragment getItem(int position) {
        	HeadlinesFragment f = new HeadlinesFragment();
            Bundle args = new Bundle();
            //this is now the offset
        	args.putInt("datepos", 6-position);
            f.setArguments(args);
            return f;
        }
    }
    /** Restore category/article selection from saved state. */
    void restoreSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //setNewsCategory(savedInstanceState.getInt("catIndex", 0));
            if (mIsDualPane) {
                int artIndex = savedInstanceState.getInt("artIndex", 0);
                mHeadlinesFragment.setSelection(artIndex);
                //!TODO handle this
                //onHeadlineSelected(artIndex, null, null);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        restoreSelection(savedInstanceState);
    }

    /** Sets up Action Bar (if present).
     *
     * @param showTabs whether to show tabs (if false, will show list).
     * @param selTab the selected tab or list item.
     */
    public void setUpActionBar(boolean showTabs, int selTab) {
        if (Build.VERSION.SDK_INT < 11) {
            // No action bar for you!
            // But do not despair. In this case the layout includes a bar across the
            // top that looks and feels like an action bar, but is made up of regular views.
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
       // setNewsCategory(0);
    }

  
    /** Called when a headline is selected.
     *
     * This is called by the HeadlinesFragment (via its listener interface) to notify us that a
     * headline was selected in the Action Bar. The way we react depends on whether we are in
     * single or dual-pane mode. In single-pane mode, we launch a new activity to display the
     * selected article; in dual-pane mode we simply display it on the article fragment.
     *
     * @param index the index of the selected headline.
     */
    @Override
    public void onHeadlineSelected(int index, String extId, ArrayList allIds, ArrayList allTitles, ArrayList allSubtitles) {

        mArtIndex = index;
        if (mIsDualPane) {
            // display it on the article fragment
            mArticleFragment.displayArticle();
        }
        else {
            // use separate activity
            Intent i = new Intent(this, ArticleActivity.class);
            i.putExtra("artIndex", index );
            i.putExtra("extId", extId );
            i.putExtra("allIds", allIds );
            i.putExtra("allTitles", allTitles );
            i.putExtra("allSubtitles", allSubtitles );
            startActivity(i);
        }
    }

    /** Save instance state. Saves current category/article index. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("artIndex", mArtIndex);
        super.onSaveInstanceState(outState);
    }


}
