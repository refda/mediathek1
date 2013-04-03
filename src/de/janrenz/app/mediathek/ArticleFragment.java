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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.janrenz.app.mediathek.HeadlinesFragment.OnHeadlineSelectedListener;

/**
 * Fragment that displays a news article.
 */
public class ArticleFragment extends Fragment {

	View mView = null;
	// The article we are to display
	NewsArticle mNewsArticle = null;

	// The id of our movie
	String extId = null;

	// The cvideo path
	String videoPath = null;

	ArrayList<String[]> videoSources = new ArrayList<String[]>();

	// Parameterless constructor is needed by framework
	public ArticleFragment() {

		super();
	}

	OnMovieClickedListener mOnMovieClickedListener = null;

	/**
	 * Represents a listener that will be notified of selections.
	 */
	public interface OnMovieClickedListener {
		/**
		 * Called when a given item is selected.
		 * 
		 * @param index
		 *            the index of the selected item.
		 * @param string
		 */
		public void onMovieSelected(String url);
	}

	/**
	 * Sets up the UI.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.detail, container, false);
		;
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		displayArticle();
	};

	public void setOnMovieClickedListener(OnMovieClickedListener listener) {
		mOnMovieClickedListener = listener;
	}

	private Integer getQualityPositionForString(String quality) {
		for (int j = 0; j < videoSources.size(); j++) {
			// for (String[] obj : videoSources) {
			// qualities.add(obj[0]);
			String[] arr = videoSources.get(j);
			if (arr[0].equals(quality)) {
				Log.v("QUALITY", quality);
				return j;
			}
		}
		return 1;
	}

	/**
	 * Displays a particular article.
	 * 
	 * @param extId
	 *            the article to display
	 */
	public void displayArticle() {
		TextView text = (TextView) mView.findViewById(R.id.headline1);
		text.setText(getArguments().getString("title"));
		TextView text2 = (TextView) mView.findViewById(R.id.headline2);
		text2.setText(getArguments().getString("subtitle"));
		Log.e("DEBUG", "http://m-service.daserste.de/appservice/1.4.1/video/"
				+ getArguments().getString("extId"));
		new AccessWebServiceTask()
				.execute("http://m-service.daserste.de/appservice/1.4.1/video/"
						+ getArguments().getString("extId"));
	}

	private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			return loadXML(urls[0]);
		}

		protected void onPostExecute(String result) {
			Log.e("DEBUG", result);
			
			//get duration
			InputSource inputSrc = new InputSource(new StringReader(result));
			inputSrc.setEncoding("UTF-8");
			XPath xpath = XPathFactory.newInstance().newXPath();
			String expression = "//playlist/video/duration";
			// list of nodes queried
			try {
				NodeList nodes = (NodeList) xpath.evaluate(expression,
						inputSrc, XPathConstants.NODESET);
				
					Node node = nodes.item(0);
					//
					String duration = node.getTextContent();
					TextView tView = (TextView) mView.findViewById(R.id.durationText);
					tView.setText(duration);
			}catch (Exception e) {
				// TODO: handle exception
			}
				//TODO: MAybe we can rewind the StringReader and reuse it
			inputSrc = new InputSource(new StringReader(result));
			inputSrc.setEncoding("UTF-8");
			// specify the xpath expression
			expression = "//playlist/video/teaserImage/variants/variant/url";
			// list of nodes queried
			try {
				NodeList nodes = (NodeList) xpath.evaluate(expression,
						inputSrc, XPathConstants.NODESET);
				for (int i = 0; i < 1; i++) {
					Node node = nodes.item(i);
					//
					String url = node.getTextContent();
					ImageView imageView = (ImageView) mView
							.findViewById(R.id.thumbnail);
					/**
					 * Set the image
					 */
					DisplayImageOptions loadingOptions = new DisplayImageOptions.Builder()
							.showStubImage(R.drawable.ic_empty)
							// .showImageForEmptyUri(R.drawable.ic_empty)
							.showImageOnFail(R.drawable.ic_error)
							.cacheInMemory()
							// .cacheOnDisc()
							.build();

					ImageView image_view = (ImageView) mView
							.findViewById(R.id.thumbnail);

					if (image_view != null) {
						ImageLoader.getInstance().displayImage(url, image_view,
								loadingOptions);
					}
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			// get the streams
			expression = "//playlist/video/assets/asset";
			inputSrc = new InputSource(new StringReader(result));
			inputSrc.setEncoding("UTF-8");
			videoSources = new ArrayList<String[]>();

			// list of nodes queried
			try {
				String tempUrl = "";
				NodeList nodes = (NodeList) xpath.evaluate(expression,
						inputSrc, XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					Boolean useThisUrl = false;
					String bandwith = null;
					String serverPrefix = null;
					NodeList nodeChilds = node.getChildNodes();
					for (int j = 0; j < nodeChilds.getLength(); j++) {
						Node childNode = nodeChilds.item(j);
						String nodeName = childNode.getNodeName();
						String nodeValue = childNode.getTextContent();
						if (nodeName.equals("recommendedBandwidth")) {
							Log.v("XML", "Bandwith " + nodeValue);
							bandwith = nodeValue;
							break;

						} else if (nodeName.equals("fileName")) {
							tempUrl = nodeValue;
							// Log.v("XML", "**** "+nodeName + " with value " +
							// nodeValue);

						} else if (nodeName.equals("serverPrefix")) {
							serverPrefix = nodeValue;
							// Log.v("XML", "**** "+nodeName + " with value " +
							// nodeValue);
						} else {
							// Log.v("XML", "Untreated Nodetype "+nodeName +
							// " with value " + nodeValue);
						}
					}
					videoPath = tempUrl;
					String videoUrl = serverPrefix + videoPath;
					if (videoUrl.startsWith("http")) {
						if (bandwith.equals("")) {
							bandwith = "HbbTV";
						}
						videoSources.add(new String[] { bandwith, videoUrl });
					}

					// }

				}

				// Spinner population
				// default quality

				ArrayList qualities = new ArrayList();
				for (String[] obj : videoSources) {
					qualities.add(obj[0]);
				}
				final Spinner s = (Spinner) mView
						.findViewById(R.id.qualitySpinner);
				ArrayAdapter<String> mspinnerAdapter = new ArrayAdapter<String>(
						getActivity(), android.R.layout.simple_spinner_item,
						qualities);
				s.setAdapter(mspinnerAdapter);
				SharedPreferences appSettings = getActivity()
						.getSharedPreferences("AppPreferences",
								getActivity().MODE_PRIVATE);
				String defaultQuality = appSettings.getString("Quality",
						"DSL768");

				videoPath = videoSources
						.get(getQualityPositionForString(defaultQuality))[1];
				s.setSelection(getQualityPositionForString(defaultQuality));

				s.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						// Integer item = s.getSelectedItemPosition();

						videoPath = videoSources.get(arg2)[1];
						SharedPreferences appSettings = getActivity()
								.getSharedPreferences("AppPreferences",
										getActivity().MODE_PRIVATE);
						SharedPreferences.Editor prefEditor = appSettings
								.edit();
						prefEditor.putString("Quality",
								videoSources.get(arg2)[0]);
						prefEditor.commit();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
				Button button = (Button) mView.findViewById(R.id.buttonWatch);

				button.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						// if (mOnMovieClickedListener != null) {

						if (videoPath != null) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.parse(videoPath),
									"video/mp4");
							startActivity(intent);
							Toast.makeText(getActivity(),
									"Lade Video " + videoPath,
									Toast.LENGTH_LONG).show();
						}

						// }
					}

				});
				Button buttonCopy = (Button) mView
						.findViewById(R.id.buttonCopy);

				buttonCopy.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						ClipboardManager clipboard = (ClipboardManager) getActivity()
								.getSystemService(
										getActivity().CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("TVEins",
								videoPath);
						clipboard.setPrimaryClip(clip);
						Toast.makeText(getActivity(),
								"Url wurde in Zwischenablage kopiert",
								Toast.LENGTH_LONG).show();
					}

				}

				);

				mView.findViewById(R.id.showAfterLoadItems).setVisibility(
						View.VISIBLE);
				mView.findViewById(R.id.hideAfterLoadItems).setVisibility(
						View.GONE);

			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Loads article data into the webview.
	 * 
	 * This method is called internally to update the webview's contents to the
	 * appropriate article's text.
	 */
	String loadXML(String URL) {
		final long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
		final File httpCacheDir = new File(getActivity().getCacheDir(), "http");
		try {
			Class.forName("android.net.http.HttpResponseCache")
					.getMethod("install", File.class, long.class)
					.invoke(null, httpCacheDir, httpCacheSize);
		} catch (Exception httpResponseCacheNotAvailable) {

		}
		StringBuilder stringBuilder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				inputStream.close();
			} else {
				Log.d("readXML", "Failed to download file");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String xml = stringBuilder.toString();
		return xml;
	}

}
