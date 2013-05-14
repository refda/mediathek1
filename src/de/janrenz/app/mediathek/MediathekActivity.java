package de.janrenz.app.mediathek;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.cketti.library.changelog.ChangeLog;
import de.janrenz.app.mediathek.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
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
public class MediathekActivity extends SherlockFragmentActivity {
	public final int MENUINFOID = 1;
	public final int MENUUPDATEID = 2;
	public final int MENUSETTINGSID = 3;
	public final int MENUSHAREID = 4;
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
	ChangeLog cl;
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
		// int catIndex = savedInstanceState == null ? 0 :
		// savedInstanceState.getInt("catIndex", 0);
		// setUpActionBar(mIsDualPane, catIndex);

		// Set up headlines fragment

		// restoreSelection(savedInstanceState);
		
		cl = new ChangeLog(this);
		//only show changelog if there are breaking changes
		//if (cl.isFirstRun()) {
		//    cl.getLogDialog().show();
		//}
		

	}

	/** Restore category/article selection from saved state. */
	void restoreSelection(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (mIsDualPane) {
				// int artIndex = savedInstanceState.getInt("artIndex", 0);
				
				// !TODO handle this
				// onHeadlineSelected(artIndex, null, null);
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
	}
	@Override
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
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
		return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//lets add some menu stuff
		
		menu.add(Menu.NONE, MENUUPDATEID, Menu.NONE, "Aktualisieren")
		.setIcon(R.drawable.ic_menu_refresh)
		.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(Menu.NONE, MENUSETTINGSID, Menu.NONE, "Einstellungen")
		.setIcon(R.drawable.ic_action_settings)
		.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_NEVER
						| MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(Menu.NONE, MENUINFOID, Menu.NONE, "Info")
		.setIcon(R.drawable.action_about)
		.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_NEVER
						| MenuItem.SHOW_AS_ACTION_NEVER);
		if (mIsDualPane) {
		menu.add(Menu.NONE, MENUSHAREID, Menu.NONE, "Teilen")
		.setIcon(R.drawable.menu_social_share)
		.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_IF_ROOM);

		}
		
		return super.onCreateOptionsMenu(menu);
	}
	  protected AlertDialog getInfoDialog() {
	        TextView tv = new TextView(this);
	        //tv.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_holo_dark)); 
	        tv.setPadding(15, 15, 15, 15);
	        tv.setMovementMethod(new ScrollingMovementMethod());
	        tv.setScrollBarStyle(1);
	        tv.setText(Html.fromHtml(getString(R.string.infotext)));
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        
	        builder.setTitle("Mediathek 1")         
	                .setView(tv)
	                .setInverseBackgroundForced(true)//needded for old android version
	                .setCancelable(false)
	                // OK button
	                .setPositiveButton(
	                        this.getResources().getString(R.string.changelog_ok_button),
	                        new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) {
	                               dialog.dismiss();
	                            }
	                        });
            // Show "More…" button if we're only displaying a partial change log.
            builder.setNegativeButton(R.string.info_popup_changelog,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                    		   if (cl != null ) cl.getFullLogDialog().show();	
                        }
                    });
	        

	        return builder.create();
	    }
	  
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENUINFOID:
			// custom dialog	
			final AlertDialog dialog = this.getInfoDialog();
			// set the custom dialog components - text, image and button
			dialog.show();
			return true;
		case MENUUPDATEID:
			//send update event to everyone who cares
			BusProvider.getInstance().post(new UpdatePressedEvent());
			return true;
		case MENUSETTINGSID:
			
			  Intent i = new Intent(this, SettingsActivity.class);
	            //i.putExtra("pos", position );
	            startActivity(i);
			return true;
		case MENUSHAREID:
			BusProvider.getInstance().post(new ShareActionSelectedEvent());	
			return true;
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this, MediathekActivity.class));
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
