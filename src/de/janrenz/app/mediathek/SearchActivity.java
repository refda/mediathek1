/*
 * Copyright (C) 2013 Jan Renz
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


import android.app.SearchManager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;

import android.widget.LinearLayout;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import java.util.ArrayList;


public class SearchActivity extends SherlockFragmentActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private SearchView searchView = null;
    public final int MENUSEARCHID = 5;

    Menu mMenu = null;
    String mQuery = "";
    GridView mGridView = null;
    // The list adapter for the list we are displaying
    SimpleCursorAdapter mListAdapter;
    private Cursor myCursor;


    // The listener we are to notify when a headline is selected

    private ArrayList<Movie> mAllItems = new ArrayList<Movie>();

    public void onStart(){
        super.onStart();
    }

    public void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.searchresults);
        mGridView = (GridView) this.findViewById(R.id.searchResultGrid);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //now lets decide the layout, note wa also use a different item in the cursor adapter
        int layoutId =   R.layout.headline_item;
        if (getResources().getBoolean(R.bool.has_two_panes)) {
            mGridView.setNumColumns(3);
            layoutId = R.layout.headline_item_grid;
        }
        mListAdapter = new RemoteImageCursorAdapter(
                this,
                layoutId, null,
                new String[] { "title", "image" }, new int[] {
                R.id.text_view,
                R.id.thumbnail });
        mGridView.setAdapter(mListAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//

                myCursor.moveToPosition(position);
                String cExtId = myCursor.getString(myCursor.getColumnIndexOrThrow("extId"));
                if (getResources().getBoolean(R.bool.has_two_panes)) {
                    // display it on the article fragment
                    try {
                        //BusProvider.getInstance().post(new MovieSelectedEvent(position, cExtId, getArguments().getInt("dateint", 0), mAllItems));
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    //this.getListView().setSelection(position);

                } else {
                }

                // use separate activity
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                i.putExtra("pos", position);
                i.putExtra("movies", mAllItems);
                i.putExtra("title", "Suchergebnisse");
                startActivity(i);
            }
        });
        super.onCreate(savedInstanceState);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //lets add some menu stuff
        searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Suche in Mediathek…");
        searchView.setOnQueryTextListener(this);
        // searchView.setOnSuggestionListener(this);

        menu.add(Menu.NONE, MENUSEARCHID, Menu.NONE, "Suche")
                .setIcon(R.drawable.ic_action_search).setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


        mMenu = menu;
        handleIntent(getIntent());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, MediathekActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        if (mMenu != null){
            MenuItem searchMenuItem = mMenu.findItem(MENUSEARCHID);
        }
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query =    mQuery =         intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);

        }
    }
    private void triggerLoad(String query, Boolean forceReload ){


        Bundle args = new Bundle();
        args.putString("query", query);

        int loaderId = 300;
        //different loader id per day by using the timestamp of the firstmobve!
        if (forceReload){
           getSupportLoaderManager().restartLoader(loaderId, args, this);
        }else{
            getSupportLoaderManager().initLoader(loaderId, args, this);
        }
    }
    private void doSearch(String queryStr) {
        try {
            this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);

        }catch(Exception e){}
        triggerLoad(queryStr, true);
        searchView.clearFocus();

        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mGridView.getApplicationWindowToken(), 0);
        getSupportActionBar().setTitle("Suche nach \"" + queryStr + "\"");

    }
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // query code
        Uri queryUri = Uri.parse("content://de.janrenz.app.mediathek.cursorloader.data");
        queryUri = queryUri.buildUpon().appendQueryParameter("method", "search").appendQueryParameter("query", mQuery).build();
        try {
            //setListShown(false);
        } catch (Exception e) {
            Log.e("ERROR_____", e.getMessage());
        }
        return new CursorLoader(
                this,
                queryUri,
                new String[] { "title", "image" , "extId", "startTime", "startTimeAsTimestamp", "isLive"},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.e("______", "loadFinished");
        Log.e("____", "count :" + cursor.getCount());
        mListAdapter.swapCursor(cursor);
        mAllItems = new ArrayList<Movie>();


        myCursor = cursor;
        for(myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
            // The Cursor is now set to the right position
            Movie mMovie = new Movie();
            mMovie.setTitle(myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
            mMovie.setSubtitle(myCursor.getString(myCursor.getColumnIndexOrThrow("subtitle")));
            mMovie.setExtId(myCursor.getString(myCursor.getColumnIndexOrThrow("extId")));
            mMovie.setStarttime(myCursor.getString(myCursor.getColumnIndexOrThrow("startTime")));
            mMovie.setStarttimestamp(myCursor.getInt(myCursor.getColumnIndexOrThrow("startTimeAsTimestamp")));
            mMovie.setIsLive(myCursor.getString(myCursor.getColumnIndexOrThrow("isLive")));

            mAllItems.add(mMovie);
        }
        try {
            this.findViewById(R.id.progressBar).setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);

        }catch(Exception e){}

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.e("______", "loadReset");
        if (myCursor != null) {
            mListAdapter.swapCursor(null);
            myCursor = null;
        }
    }

    /** if a user enters some text in the search field in the action bar **/
    @Override
    public boolean onQueryTextSubmit(String query) {
        mQuery = query;
        doSearch(query);
        mMenu.findItem(MENUSEARCHID).collapseActionView();

        return true;
    }
    @Override
    public boolean onQueryTextChange(String query) {
        //kind of a hack to close the action view if a users clicks on the x,
        if (query.equalsIgnoreCase("")){
           // MenuItem searchMenuItem = mMenu.findItem(MENUSEARCHID);
            mQuery = query;
            triggerLoad(query, true);
        }
        return true;
    }

}
