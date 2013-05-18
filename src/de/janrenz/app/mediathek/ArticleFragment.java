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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

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

import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;

/**
 * Fragment that displays a news article.
 */
public class ArticleFragment extends Fragment {

	View mView = null;
	// The article we are to display

	// The id of our movie
	String extId = null;

	// The cvideo path
	String videoPath = null;

	String title = "";
	ArrayList<String[]> videoSources = new ArrayList<String[]>();

	// Parameterless constructor is needed by framework
	public ArticleFragment() {
		super();
	}

	public void setExtId(String id) {
		extId = id;
	}

	public String getExtId() {
		return extId;
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
		try {
			displayArticle();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return mView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BusProvider.getInstance().register(this);
		return;
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	};

	public void setOnMovieClickedListener(OnMovieClickedListener listener) {
		mOnMovieClickedListener = listener;
	}
	
	public Boolean shareMovieUrl(){
		if (videoPath != null){
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, videoPath);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, this.title);
			sendIntent.setType("text/plain");
			startActivity(sendIntent);
			return true;
		}else{
			return false;
			//nothing to show here, maybe show toast
		}
	}
	
	public Integer getQualityPositionForString(String quality) {
		for (int j = 0; j < videoSources.size(); j++) {
			String[] arr = videoSources.get(j);
			if (arr[0].equals(quality)) {
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
		//display the information we already got, then fetch the detail information 
		TextView text = (TextView) mView.findViewById(R.id.headline1);
		text.setText(getArguments().getString("title"));
		TextView text2 = (TextView) mView.findViewById(R.id.headline2);
		text2.setText(getArguments().getString("subtitle"));
		TextView text3 = (TextView) mView.findViewById(R.id.senderinfo);
		text3.setText(getArguments().getString("senderinfo"));
        this.extId = getArguments().getString("extId");
		this.title = getArguments().getString("title");
        //http://m-service.daserste.de/appservice/1.4.0/image/video/das-morgenmagazin-ueber-den-tatort-mit-nora-tschirner-und-christian-ulmen-100/jpg/257
        int jpegWidth = 257;
        if (getResources().getBoolean(R.bool.has_two_panes)) {
            jpegWidth = 1024;
        }
        //images are not vailable in 1.4.1 api
        String url = "http://m-service.daserste.de/appservice/1.4.0/image/video/" + this.extId + "/jpg/" + jpegWidth;
        DisplayImageOptions loadingOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.ic_empty)
                        // .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory()
                //TODO Handle out of memory exceptions!
                        .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                        // .cacheOnDisc()
                .build();

        ImageView image_view = (ImageView) mView
                .findViewById(R.id.thumbnail);

        if (image_view != null) {
            ImageLoader.getInstance().displayImage(url, image_view,
                    loadingOptions);
        }
		new AccessWebServiceTask()
				.execute("http://m-service.daserste.de/appservice/1.4.1/video/"
						+ getArguments().getString("extId"));
	}

	private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			return loadXML(urls[0]);
		}

		protected void onPostExecute(String result) {

			// get duration
			InputSource inputSrc = new InputSource(new StringReader(result));
			inputSrc.setEncoding("UTF-8");
			XPath xpath = XPathFactory.newInstance().newXPath();
			String expression = "//playlist/video/duration";
			// list of nodes queried
			try {
				NodeList nodes = (NodeList) xpath.evaluate(expression,
						inputSrc, XPathConstants.NODESET);
				Node node = nodes.item(0);
				String duration = node.getTextContent();
				TextView tView = (TextView) mView
						.findViewById(R.id.durationText);
				tView.setText(duration);
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("onPostExecute", "failed 1: "  + e.getMessage() + result);
				return;
			}


			// get the streams
			expression = "//playlist/video/assets/asset";
			inputSrc = new InputSource(new StringReader(result));
			inputSrc.setEncoding("UTF-8");
		
			// list of nodes queried
			try {
				Boolean canHandleRtmp = canDisplayRtsp(getActivity());
				String tempUrl = "";
				NodeList nodes = (NodeList) xpath.evaluate(expression,
						inputSrc, XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					String bandwidth = null;
					String serverPrefix = null;
					NodeList nodeChilds = node.getChildNodes();
					for (int j = 0; j < nodeChilds.getLength(); j++) {
						Node childNode = nodeChilds.item(j);
						String nodeName = childNode.getNodeName();
						String nodeValue = childNode.getTextContent();
						if (nodeName.equals("recommendedBandwidth")) {
							bandwidth = nodeValue;
							break;

						} else if (nodeName.equals("fileName")) {
							tempUrl = nodeValue;
						} else if (nodeName.equals("serverPrefix")) {
							serverPrefix = nodeValue;
						} else {
						}
					}
					videoPath = tempUrl;
					String videoUrl = serverPrefix + videoPath;
					if (bandwidth.equals("")) {
						bandwidth = "HbbTV";
					}
					if (videoUrl.startsWith("http")) {
						bandwidth = bandwidth + " (MP4)";
						if (!videoUrl.equals("")) {
							videoSources
									.add(new String[] { bandwidth, videoUrl });
						}
					} else if (canHandleRtmp){
						bandwidth = bandwidth + " (RTMP)";
					}

					// }

				}

				// Spinner population
				// default quality

				ArrayList<String> qualities = new ArrayList<String>();
				for (String[] obj : videoSources) {
					qualities.add(obj[0]);
				}
				final Spinner s = (Spinner) mView
						.findViewById(R.id.qualitySpinner);
				if (s == null || getActivity() == null) {
					//this might be a background taskfinsihed while the ui is long gone
					return;
				}
				ArrayAdapter<String> mspinnerAdapter = new ArrayAdapter<String>(
						getActivity(),
						android.R.layout.simple_spinner_dropdown_item,
						qualities);
				s.setAdapter(mspinnerAdapter);
				SharedPreferences appSettings = getActivity()
						.getSharedPreferences("AppPreferences",
								getActivity().MODE_PRIVATE);
				String defaultQuality = appSettings.getString("Quality",
						"DSL 1500 (MP4)");

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
							String mime = "video/mp4";
							if (videoPath.startsWith("http")) {

							} else {
								mime = "video/rtsp";
							}
							intent.setDataAndType(Uri.parse(videoPath), mime);
							if (title != null) {
								intent.putExtra(Intent.EXTRA_SUBJECT, title);
								
							}
							try {
								startActivity(intent);								
							} catch (Exception e) {
								Log.e("e", e.getMessage());
								// Kein passender IntentHandler gefunden
								 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							        
							        builder.setTitle("Fehler")         
							                .setMessage("Auf diesem Smartphone kann die URL "+ videoPath +" mit dem Dateityp "+ mime + " nicht geöffnet werden. Bitte lade Dir ein passenden Player herunter.")
							                .setInverseBackgroundForced(true)//needded for old android version
							                .setCancelable(true)
							                // OK button
							                .setPositiveButton(
							                       getActivity().getResources().getString(R.string.changelog_ok_button),
							                        new DialogInterface.OnClickListener() {
							                            @Override
							                            public void onClick(DialogInterface dialog, int which) {
							                               dialog.dismiss();
							                            }
							                        });
						            // Show "More…" button if we're only displaying a partial change log.
						     
							        

							       AlertDialog dialog = builder.create();
							       dialog.show();
							}
							//Toast.makeText(getActivity(),
							//		"Lade Video " + videoPath,
							//		Toast.LENGTH_LONG).show();
						}

					}

				});
				Button buttonCopy = (Button) mView
						.findViewById(R.id.buttonCopy);
				
				if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
					
					buttonCopy.setOnClickListener(new View.OnClickListener() {
						
						@TargetApi(Build.VERSION_CODES.HONEYCOMB) public void onClick(View v) {
							ClipboardManager clipboard = (ClipboardManager) getActivity()
									.getSystemService(
											getActivity().CLIPBOARD_SERVICE);
							ClipData clip = ClipData.newPlainText("Mediathek",
									videoPath);
							clipboard.setPrimaryClip(clip);
							Toast.makeText(getActivity(),
									"Url wurde in Zwischenablage kopiert",
									Toast.LENGTH_LONG).show();
						}
						
					});
				    
				}else{
					buttonCopy.setVisibility(View.GONE);
				} 
				//set invisble on app settings
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
				Boolean hideCopyButtonSetting = sharedPref.getBoolean(SettingsActivity.HIDE_COPYBUTTON, false);
				if (hideCopyButtonSetting == true){
					
					buttonCopy.setVisibility(View.GONE);
				}

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
	 * Check if the supplied context can handle a certain format
	 * http://stackoverflow.com/questions/2784847/how-do-i-determine-if-android-can-handle-pdf
	 *
	 * @param context
	 * @return
	 */
	public static boolean canDisplayRtsp(Context context) {
		if (context == null){
			return false;
		}
	    PackageManager packageManager = context.getPackageManager();
	    Intent testIntent = new Intent(Intent.ACTION_VIEW);
	    testIntent.setType("video/rtsp");
	   
	    //testIntent.setData(Uri.parse("rtsp://mystream"));
	    if (packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
	        return true;
	    } else {
	        return false;
	    }
	}
	/**
	 * Loads article data into the webview.
	 * 
	 * This method is called internally to update the webview's contents to the
	 * appropriate article's text.
	 */
	String loadXML(String url) {
		String result = "";
		try {
			 result = doHttpUrlConnectionAction (url);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
		
	}
	/**
	   * Returns the output from the given URL.
	   * 
	   * I tried to hide some of the ugliness of the exception-handling
	   * in this method, and just return a high level Exception from here.
	   * Modify this behavior as desired.
	   * 
	   * @param desiredUrl
	   * @return
	   * @throws Exception
	   */
	  private String doHttpUrlConnectionAction(String desiredUrl)
	  throws Exception
	  {
	    URL url = null;
	    BufferedReader reader = null;
	    StringBuilder stringBuilder;

	    try
	    {
	      // create the HttpURLConnection
	      url = new URL(desiredUrl);
	      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	      
	      // just want to do an HTTP GET here
	      connection.setRequestMethod("GET");
	      
	      // uncomment this if you want to write output to this url
	      //connection.setDoOutput(true);
	      
	      // give it 15 seconds to respond
	      connection.setReadTimeout(15*1000);
	      connection.connect();

	      // read the output from the server
	      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	      stringBuilder = new StringBuilder();

	      String line = null;
	      while ((line = reader.readLine()) != null)
	      {
	        stringBuilder.append(line + "\n");
	      }
	      return stringBuilder.toString();
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	      throw e;
	    }
	    finally
	    {
	      // close the reader; this can throw an exception too, so
	      // wrap it in another try/catch block.
	      if (reader != null)
	      {
	        try
	        {
	          reader.close();
	        }
	        catch (IOException ioe)
	        {
	          ioe.printStackTrace();
	        }
	      }
	    }
	  }

}
