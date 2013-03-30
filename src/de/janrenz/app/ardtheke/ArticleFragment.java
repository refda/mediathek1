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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
 
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.loopj.android.image.SmartImageView;

import de.janrenz.app.ardtheke.HeadlinesFragment.OnHeadlineSelectedListener;

/**
 * Fragment that displays a news article.
 */
public class ArticleFragment extends Fragment{
	
	View mView = null;
	// The article we are to display
	NewsArticle mNewsArticle = null;

	// The id of our movie
	String extId = null;

	// The cvideo path 
	String videoPath = null;
	
	List<String[]> videoSources = new ArrayList<String[]>();
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
		mView = inflater.inflate(R.layout.detail, container, false);;
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		displayArticle();
	};
	
	public void setOnMovieClickedListener(
			OnMovieClickedListener listener) {
		mOnMovieClickedListener = listener;
	}
	/**
	 * Displays a particular article.
	 * 
	 * @param extId
	 *            the article to display
	 */
	public void displayArticle() {

		new AccessWebServiceTask().execute("http://m-service.daserste.de/appservice/1.4.1/video/" + getArguments().getString("extId"));
	}

	private class AccessWebServiceTask extends AsyncTask <String, Void, String> {
		protected String doInBackground(String... urls) {
			return loadXML(urls[0]);
		}
		protected void onPostExecute(String result) { 
			 Log.v("*XMLLOADER*", result);
			InputSource inputSrc = new InputSource(new StringReader(result));
			//Toast.makeText(getActivity().getBaseContext(), result, Toast.LENGTH_LONG).show();
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			// specify the xpath expression
			String expression = "//playlist/video/teaserImage/variants/variant/url";
			// list of nodes queried
			try {
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
			    for (int i = 0; i < nodes.getLength(); i++) {
		            Node node = nodes.item(i);
		            //
		            String url = node.getTextContent();
		            Log.v("**", url);
		            SmartImageView imageView = (SmartImageView) mView.findViewById(R.id.thumbnail);
		            imageView.setImageUrl(url);
			    }
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			expression = "//playlist/video/title";
			inputSrc = new InputSource(new StringReader(result));
			// list of nodes queried
			try {
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
		            Node node = nodes.item(0);
		            String title = node.getTextContent();
		            Log.v("**", title);
		            TextView text = (TextView) mView.findViewById(R.id.headline1);
		            text.setText(title);
		           			 
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//get headline 2
			expression = "//playlist/video/broadcast";
			inputSrc = new InputSource(new StringReader(result));
			// list of nodes queried
			try {
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
		            Node node = nodes.item(0);
		            String title2 = node.getTextContent();
		            Log.v("**", title2);
		            TextView text2 = (TextView) mView.findViewById(R.id.headline2);
		            text2.setText(title2);
		           			 
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//get the streams
			expression = "//playlist/video/assets/asset";
			inputSrc = new InputSource(new StringReader(result));
			inputSrc.setEncoding("UTF-8");
			videoSources = new ArrayList<String[]>();
			// list of nodes queried
			try {
				String tempUrl = "";
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
			    for (int i = 0; i < nodes.getLength(); i++) {
		            Node node = nodes.item(i);
		            Boolean useThisUrl = false;
		            String bandwith = null;
		            String serverPrefix = null;
		            NodeList nodeChilds = node.getChildNodes();
		            for (int j = 0; j < nodeChilds.getLength(); j++) {
		            	Node childNode = nodeChilds.item(j);
		            	String nodeName =  childNode.getNodeName();
		            	String nodeValue = childNode.getTextContent();
						if (nodeName.equals("recommendedBandwidth"))
						{
							Log.v("XML", "Bandwith "+nodeValue);
							bandwith = nodeValue;
							break;
							
						}else if(nodeName.equals("fileName")){
							tempUrl = nodeValue;	
							Log.v("XML", "**** "+nodeName + " with value " + nodeValue);
		            
			            }else if(nodeName.equals("serverPrefix")){
			            	serverPrefix = nodeValue;	
							Log.v("XML", "**** "+nodeName + " with value " + nodeValue);
		            	}else
						{
							//Log.v("XML", "Untreated Nodetype "+nodeName + " with value " + nodeValue);
						}
		            }
		            
		            Log.v("XML", useThisUrl.toString());
		            //if (useThisUrl){
		            	Log.v("XML" , "Set url to" + tempUrl);
		            	videoPath = tempUrl;
		            	String videoUrl =  serverPrefix + videoPath;
		            	if (videoUrl.startsWith("http"))
		            	{
		            		if (bandwith.equals(""))
		            		{
		            			bandwith = "HbbTV (SmartTV)";
		            		}
		            		videoSources.add( new String[] {bandwith, videoUrl}); 
		            	}
		            	
		            //}
		           
			    }
			    //set qualitySeekBar
			    SeekBar seekbar =  (SeekBar) mView.findViewById(R.id.qualitySeekBar);
			    seekbar.setMax(videoSources.size()-1);
			    seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						// TODO Auto-generated method stub
						TextView qualityText = (TextView) mView.findViewById(R.id.qualityText);
						String newQualityText = videoSources.get(progress)[0];
						qualityText.setText(newQualityText);
						videoPath = videoSources.get(progress)[1];
					}
				});
					
			    Button button = (Button) mView.findViewById(R.id.button1);
		        
		        button.setOnClickListener(new View.OnClickListener() {

		            public void onClick(View v) {
		               //if (mOnMovieClickedListener != null) {
		            	   SeekBar seekbar =  (SeekBar) mView.findViewById(R.id.qualitySeekBar);
		            	   String url = videoSources.get(seekbar.getProgress())[1];
		            	   if (url != null){
		           			Intent intent = new Intent(Intent.ACTION_VIEW); 
		           			intent.setDataAndType(Uri.parse(url), "video/mp4");
		           			startActivity(intent);
		           			Toast.makeText(getActivity(), "Lade Video " + url, Toast.LENGTH_LONG).show();
		           		}
		               //}
		            }
		            
		        });
		           			 
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
						new InputStreamReader(inputStream));
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
