package de.janrenz.app.mediathek;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.janrenz.app.mediathek.R;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Main activity: shows headlines list and articles, if layout permits.
 * 
 * This is the main activity of the application. It can have several different
 * layouts depending on the SDK version, screen size and orientation. The
 * configurations are divided in two large groups: single-pane layouts and
 * dual-pane layouts.
 * 
 * In single-pane mode, this activity shows a list of headlines using a
 * {@link HeadlinesFragment}. When the user clicks on a headline, a separate
 * activity (a {@link ArticleActivity}) is launched to show the news article.
 * 
 * In dual-pane mode, this activity shows a {@HeadlinesFragment
 * } on the left side and an {@ArticleFragment
 * } on the right side. When the user selects a headline on the
 * left, the corresponding article is shown on the right.
 */
public class NewsReaderActivity extends SherlockFragmentActivity {
	public final int MENUINFOID = 1;
	// Whether or not we are in dual-pane mode
	boolean mIsDualPane = false;

	// The fragment where the headlines are displayed
	HeadlinesFragment mHeadlinesFragment;

	// The fragment where the article is displayed (null if absent)
	ArticlePagerFragment mArticleFragment;

	// The news category and article index currently being displayed
	int mCatIndex = 0;
	int mArtIndex = 0;

	// List of category titles

	ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		// find our fragments
		mHeadlinesFragment = (HeadlinesFragment) getSupportFragmentManager()
				.findFragmentById(R.id.pager);
		mArticleFragment = (ArticlePagerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.article);

		// Determine whether we are in single-pane or dual-pane mode by testing
		// the visibility
		// of the article view.
		View articleView = findViewById(R.id.article);
		mIsDualPane = articleView != null
				&& articleView.getVisibility() == View.VISIBLE;
		// Set up the Action Bar (or not, if one is not available)
		// int catIndex = savedInstanceState == null ? 0 :
		// savedInstanceState.getInt("catIndex", 0);
		// setUpActionBar(mIsDualPane, catIndex);

		// Set up headlines fragment

		// restoreSelection(savedInstanceState);

	}

	/** Restore category/article selection from saved state. */
	void restoreSelection(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			// setNewsCategory(savedInstanceState.getInt("catIndex", 0));
			if (mIsDualPane) {
				// int artIndex = savedInstanceState.getInt("artIndex", 0);
				// mHeadlinesFragment.setSelection(artIndex);
				// !TODO handle this
				// onHeadlineSelected(artIndex, null, null);
			}
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		restoreSelection(savedInstanceState);
	}

	/**
	 * Sets up Action Bar (if present).
	 * 
	 * @param showTabs
	 *            whether to show tabs (if false, will show list).
	 * @param selTab
	 *            the selected tab or list item.
	 */
	public void setUpActionBar(boolean showTabs, int selTab) {
		// if (Build.VERSION.SDK_INT < 11) {
		// No action bar for you!
		// But do not despair. In this case the layout includes a bar across the
		// top that looks and feels like an action bar, but is made up of
		// regular views.
		return;
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//lets add some menu stuff
		menu.add(Menu.NONE, MENUINFOID, Menu.NONE, "Info")
				.setIcon(R.drawable.action_about)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENUINFOID:
			// custom dialog
						final Dialog dialog = new Dialog(this);
						dialog.setContentView(R.layout.info);
						dialog.setTitle("Mediathek 1");
			 
						// set the custom dialog components - text, image and button
						
						TextView textView = (TextView) dialog.findViewById(R.id.textView1);
						textView.setText(Html.fromHtml(getString(R.string.infotext)));
						Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
						// if button is clicked, close the custom dialog
						dialogButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
			 
						dialog.show();
			return true;

		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					NewsReaderActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	

	@Override
	public void onStart() {
		super.onStart();
	}

	/** Save instance state. Saves current category/article index. */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("artIndex", mArtIndex);
		super.onSaveInstanceState(outState);
	}

}
