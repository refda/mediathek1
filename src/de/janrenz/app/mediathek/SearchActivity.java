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

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;


public class SearchActivity extends SherlockFragmentActivity implements SearchView.OnQueryTextListener {

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.movielist);


    }


    private SearchView searchView = null;
    public final int MENUSEARCHID = 5;

    Menu mMenu = null;
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

        setIntent(intent);

        handleIntent(intent);

    }

    public void onListItemClick(ListView l, View v, int position, long id) {

        // call detail activity for clicked entry

    }

    private void handleIntent(Intent intent) {

        if (mMenu != null){
            MenuItem searchMenuItem = mMenu.findItem(MENUSEARCHID);
            //set text if there is onw
            searchMenuItem.expandActionView();
        }
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query =            intent.getStringExtra(SearchManager.QUERY);
            //display the query
            searchView.setQuery(query, false);
            // and finally search it
            doSearch(query);

        }
    }

    private void doSearch(String queryStr) {
        Log.v("___", queryStr);
    }

    /** if a user enters some text in the search field in the action bar **/
    @Override
    public boolean onQueryTextSubmit(String query) {

        return true;
    }
    @Override
    public boolean onQueryTextChange(String query) {
        //kind of a hack to close the action view if a users clicks on the x,
        if (query.equalsIgnoreCase("")){
           // MenuItem searchMenuItem = mMenu.findItem(MENUSEARCHID);
            searchView.clearFocus();
        }
        return true;
    }
}
