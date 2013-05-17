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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragmentActivity;


public class SearchActivity extends SherlockFragmentActivity {

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        handleIntent(getIntent());

    }



    public void onNewIntent(Intent intent) {

        setIntent(intent);

        handleIntent(intent);

    }

    public void onListItemClick(ListView l, View v, int position, long id) {

        // call detail activity for clicked entry

    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query =            intent.getStringExtra(SearchManager.QUERY);

            doSearch(query);

        }

    }

    private void doSearch(String queryStr) {
        Log.v("___", queryStr);
    }
}
