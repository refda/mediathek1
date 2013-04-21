package de.janrenz.app.mediathek;

import java.util.ArrayList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.LinePageIndicator;

/**
 * Fragment that displays a news article.
 */
public class ArticlePagerFragment extends Fragment {

	MyAdapter mAdapter;
	ViewPager mPager;

	// Parameterless constructor is needed by framework
	public ArticlePagerFragment() {
		super();
	}

	public static ArticlePagerFragment newInstance(int pos,
			ArrayList<Movie> allItems) {
		ArticlePagerFragment f = new ArticlePagerFragment();
		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("pos", pos);
		args.putParcelableArrayList("movies", allItems);
		f.setArguments(args);
		return f;
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

	@Subscribe
	public void onMovieSelected(MovieSelectedEvent event) {
		if (mAdapter != null) {
			mAdapter.setAllItems(event.mList);
			mPager.setCurrentItem(event.pos);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new MyAdapter(getFragmentManager());
		mPager = (ViewPager) getActivity().findViewById(R.id.singlepager);
		mPager.setAdapter(mAdapter);
		try {
			if (mAdapter != null
					&& getArguments().getParcelableArrayList("movies") != null) {
				ArrayList<Movie> movies = getArguments()
						.getParcelableArrayList("movies");
				mAdapter.setCount(movies.size());
				mAdapter.setAllItems(movies);
				mPager = (ViewPager) getActivity().findViewById(
						R.id.singlepager);
				if (mPager != null) {
					mPager.setCurrentItem(getArguments().getInt("pos", 0));
				}
				// Set the pager with an adapter
				// Bind the title indicator to the adapter
				LinePageIndicator titleIndicator = (LinePageIndicator) getActivity()
						.findViewById(R.id.detailpageindicator);
				titleIndicator.setViewPager(mPager);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	// mPager.setCurrentItem(0);
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// check if there is an activity with all the data we need
		View v = inflater.inflate(R.layout.detailpager, container, false);
		return v;
	}

	public static class MyAdapter extends FragmentStatePagerAdapter {
		private int mcount = 0;
		private ArrayList<Movie> mallItems;
		private ArrayList<String> extIds;

		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		public void setCount(int newCount) {
			mcount = newCount;
		}

		public void setAllItems(ArrayList<Movie> allItems) {
			mallItems = allItems;
			extIds = new ArrayList<String>();
			for (Movie movie : allItems) {
				extIds.add(movie.getExtId());
			}
			this.setCount(allItems.size());
			this.notifyDataSetChanged();
		}

		public String getPageTitle(int position) {
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
			args.putString("extId", mallItems.get(position).getExtId());
			args.putString("title", mallItems.get(position).getTitle());
			args.putString("subtitle", mallItems.get(position).getSubtitle());
			f.setArguments(args);
			// we need to have this availbe from the outside as well
			f.setExtId(mallItems.get(position).getExtId());
			return f;
		}

		// http://stackoverflow.com/questions/10849552/android-viewpager-cant-update-dynamically
		public int getItemPosition(Object item) {
			ArticleFragment fragment = (ArticleFragment) item;
			String title = fragment.getExtId();
			int position = extIds.indexOf(title);

			if (position >= 0) {
				return position;
			} else {
				return POSITION_NONE;
			}
		}
	}
}
