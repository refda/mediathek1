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

import android.content.Loader.OnLoadCompleteListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import de.janrenz.app.ardtheke.R;

/**
 * Fragment that displays the news headlines for a particular news category.
 * 
 * This Fragment displays a list with the news headlines for a particular news
 * category. When an item is selected, it notifies the configured listener that
 * a headlines was selected.
 */
public class HeadlinesFragment extends ListFragment implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	// The list adapter for the list we are displaying
	SimpleCursorAdapter mListAdapter;

	private Cursor myCursor;
	// The listener we are to notify when a headline is selected
	OnHeadlineSelectedListener mHeadlineSelectedListener = null;
	private static  int LOADER_ID = 0x02;
	private ArrayList<String> mArrayList = new ArrayList<String>();
	private ArrayList<String> mSubtitles = new ArrayList<String>();
	private ArrayList<String> mTitles    = new ArrayList<String>();
	/**
	 * Represents a listener that will be notified of headline selections.
	 */
	public interface OnHeadlineSelectedListener {
		/**
		 * Called when a given headline is selected.
		 * 
		 * @param index
		 *            the index of the selected headline.
		 * @param string 
		 * 
		 * @param allids
		 * 
		 * @param title
		 * 
		 * @param subtitle
		 */
		public void onHeadlineSelected(int index, String string, ArrayList all,  ArrayList allTitles, ArrayList allSubtitles);
	}

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
//		loadCategory(0);
	}

	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
		setListShown(false);
        Log.d("TAG", "onViewCreated");
        //...do something
        Bundle args = new Bundle();
        args.putInt("datepos", this.getArguments().getInt("datepos", 0));
        //note that we need a different loader id for each loader
        getActivity().getSupportLoaderManager().initLoader(this.getArguments().getInt("datepos", 0)+LOADER_ID, args, this);
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		Log.v("CURSOR", "called with i "+i + " and " +getArguments().getInt("datepos", 0));
		Uri queryUri = Uri.parse("content://de.janrenz.app.ardtheke.cursorloader.data");
		Integer offset =  getArguments().getInt("datepos", 0);
		queryUri = queryUri.buildUpon().appendQueryParameter("offset", offset.toString()).build();
		try {
			
			setListShown(false);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return new CursorLoader(
				getActivity(),
				queryUri,
				new String[] { "title", "image" , "extId"}, 
				null, 
				null, 
				null);
	}

	/**
	 * Sets the listener that should be notified of headline selection events.
	 * 
	 * @param listener
	 *            the listener to notify.
	 */
	public void setOnHeadlineSelectedListener(
			OnHeadlineSelectedListener listener) {
		mHeadlineSelectedListener = listener;
	}

	public void onResume() {
		setListShown(true);
		super.onResume();
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		setOnHeadlineSelectedListener((OnHeadlineSelectedListener) getActivity());
		mListAdapter.swapCursor(cursor);
		myCursor = cursor;
		
		for(myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
		    // The Cursor is now set to the right position
		    mArrayList.add( myCursor.getString(myCursor.getColumnIndexOrThrow("extId")));
		    mTitles.add( myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
		    mSubtitles.add( myCursor.getString(myCursor.getColumnIndexOrThrow("subtitle")));
		}
		try {
			setListShown(true);		
		} catch (Exception e) {
			// TODO: handle exception
		}
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
		if (null != mHeadlineSelectedListener) {
			myCursor.moveToPosition(position);
			//Log.v ("DEBUG ", myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
			mHeadlineSelectedListener.onHeadlineSelected(
					position,  
					myCursor.getString(myCursor.getColumnIndexOrThrow("extId")), 
					mArrayList,
					mTitles,
					mSubtitles
					);
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
