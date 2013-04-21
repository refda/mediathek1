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

import android.content.Intent;
import android.content.Loader.OnLoadCompleteListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;

import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import de.janrenz.app.mediathek.R;

/**
 * Fragment that displays the news headlines for a particular news category.
 * 
 * This Fragment displays a list with the news headlines for a particular news
 * category. When an item is selected, it notifies the configured listener that
 * a headlines was selected.
 */
public class HeadlinesFragment extends SherlockListFragment implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	// The list adapter for the list we are displaying
	SimpleCursorAdapter mListAdapter;
	private Boolean isLoading = true;
	private Cursor myCursor;
	// The listener we are to notify when a headline is selected
	private static  int LOADER_ID = 0x02;
	private ArrayList<Movie> mAllItems = new ArrayList<Movie>();
    private int scrollPos;
	/**
	 * Default constructor required by framework.
	 */
	public HeadlinesFragment() {
		super();
	}

	@Override
	public void onStart() {
		super.onStart();
		setListAdapter(mListAdapter);
		getListView().setOnItemClickListener(this);
		setListShown(false);
	}

	
	public void reloadAllVisisble() { 
		try {
			mListAdapter.notifyDataSetChanged();
			triggerLoad();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
		this.setEmptyText("Keine Einträge gefunden.");
		setListShown(false);
		if (getResources().getBoolean(R.bool.has_two_panes)) {
			this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			//this.getListView().setDrawSelectorOnTop(true);
		}
        triggerLoad();
}
	private void triggerLoad(){
		setListShown(false);
		this.isLoading = true;
	    Bundle args = new Bundle();
	    args.putInt("datepos", this.getArguments().getInt("datepos", 0));
	    //note that we need a different loader id for each loader
	    getActivity().getSupportLoaderManager().initLoader(this.getArguments().getInt("datepos", 0)+LOADER_ID, args, this);

}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		// mListAdapter = new ArrayAdapter<String>(getActivity(),
		// R.layout.headline_item,
		// mHeadlinesList);
		mListAdapter = new RemoteImageCursorAdapter(getActivity(),
				R.layout.headline_item, null,
				new String[] { "title", "image" }, new int[] { R.id.text_view,
						R.id.thumbnail });
		// ListView listView = (ListView) findViewById(R.id.list);
		this.setListAdapter(mListAdapter);
		
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		
		// query code
		Uri queryUri = Uri.parse("content://de.janrenz.app.mediathek.cursorloader.data");
		Integer offset =  getArguments().getInt("datepos", 0);
		queryUri = queryUri.buildUpon().appendQueryParameter("offset", offset.toString()).build();
		try {
			
			setListShown(false);
		} catch (Exception e) {
			// 
		}
		return new CursorLoader(
				getActivity(),
				queryUri,
				new String[] { "title", "image" , "extId"}, 
				null, 
				null, 
				null);
	}

	@Override
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
		if ( this.isLoading == false ){
			setListShown(true);
			if (this.getListView().getCount()== 0){
				triggerLoad();
			}
		}else{
			
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}
	
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
			if (cursor != null && cursor.getCount()>0){
			mAllItems = new ArrayList<Movie>();
			mListAdapter.swapCursor(cursor);
			myCursor = cursor;
			for(myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
				// The Cursor is now set to the right position
				Movie mMovie = new Movie();
				mMovie.setTitle(myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
				mMovie.setSubtitle(myCursor.getString(myCursor.getColumnIndexOrThrow("subtitle")));
				mMovie.setExtId(myCursor.getString(myCursor.getColumnIndexOrThrow("extId")));
				mAllItems.add(mMovie);
			}
		}
		try {
			setListShown(true);		
		} catch (Exception e) {
			// TODO: handle exception
		}
		this.isLoading = false;
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		//setListShown(true);
		mListAdapter.swapCursor(null);
		myCursor = null;
	}

	 
	
	/**
	 * Handles a click on a headline.
	 * 
	 * This causes the configured listener to be notified that a headline was
	 * selected.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
			myCursor.moveToPosition(position);
			String cExtId = myCursor.getString(myCursor.getColumnIndexOrThrow("extId"));
			if (getResources().getBoolean(R.bool.has_two_panes)) {
	            // display it on the article fragment
				 BusProvider.getInstance().post(new MovieSelectedEvent(position, cExtId, mAllItems));
				 //this.getListView().setSelection(position);
			
	        }
	        else {
	            // use separate activity
	            Intent i = new Intent(getActivity(), ArticleActivity.class);
	            i.putExtra("pos", position );
	            i.putExtra("movies",mAllItems );
	            startActivity(i);
	        }
		
	}

	/**
	 * Sets choice mode for the list
	 * 
	 * @param selectable
	 *            whether list is to be selectable.
	 */
	public void setSelectable(boolean selectable) {
		if (selectable) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		} else {
			getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
		}
	}
}
