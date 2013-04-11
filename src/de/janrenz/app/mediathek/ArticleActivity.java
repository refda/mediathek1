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

package de.janrenz.app.mediathek;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.LinePageIndicator;

import de.janrenz.app.mediathek.R;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
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
public class ArticleActivity extends SherlockFragmentActivity {
    // The news category index and the article index for the article we are to display
    int mCatIndex, mArtIndex;
    //the external id
    String extId;
    ArrayList<Movie> allItems;

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
        setContentView(R.layout.detailactivity);
        mCatIndex = getIntent().getExtras().getInt("catIndex", 0);
        mArtIndex = getIntent().getExtras().getInt("artIndex", 0);
        extId     = getIntent().getExtras().getString("extId");
        allItems = getIntent().getExtras().getParcelableArrayList("allItems");
      
        
        // If we are in two-pane layout mode, this activity is no longer necessary
        if (getResources().getBoolean(R.bool.has_two_panes)) {
        	Log.v("DEBUG", "We are in two panes mpde, no need to go any further here");
            finish();
            return;
        }
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.detail_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            
      Log.v("ÜÜÜÜÜ", "*****");
      Log.v("ÜÜÜÜÜ", "*****" + getSupportFragmentManager());
      Log.v("ÜÜÜÜÜ", "*****");
            // During initial setup, plug in the details fragment.
            Fragment details = new ArticlePagerFragment();
            Bundle args = getIntent().getExtras();
            int test = args.getInt("pos", 7);
            details.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.detail_fragment_container, details).commit();
            //getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }
        
        
        setContentView( R.layout.detailactivity );
          
      
    }
    




	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
            menu.add("Text")
                .setIcon(R.drawable.abs__ic_menu_share_holo_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
           

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this, NewsReaderActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
   
}
