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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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


/**
 * Fragment that displays a news article.
 */
public class ArticleFragment extends Fragment{
	// The webview where we display the article (our only view)
	WebView mWebView;

	// The article we are to display
	NewsArticle mNewsArticle = null;

	// The id of our movie
	String extId = null;

	// The cvideo path 
	String videoPath = null;
	// Parameterless constructor is needed by framework
	public ArticleFragment() {
		super();
	}

	/**
	 * Sets up the UI. It consists if a single WebView.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.detail, container, false);
	}

	/**
	 * Displays a particular article.
	 * 
	 * @param extId
	 *            the article to display
	 */
	public void displayArticle(String extId) {
		// mNewsArticle = extId;
		this.extId = extId;
		new AccessWebServiceTask().execute("http://m-service.daserste.de/appservice/1.4.1/video/" + extId);
	
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
		            SmartImageView imageView = (SmartImageView) getActivity().findViewById(R.id.thumbnail);
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
		            TextView text = (TextView) getActivity().findViewById(R.id.headline1);
		            text.setText(title);
		           			 
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//get the streams
			expression = "//playlist/video/assets/asset";
			inputSrc = new InputSource(new StringReader(result));
			// list of nodes queried
			try {
				String tempUrl = "";
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
			    for (int i = 0; i < nodes.getLength(); i++) {
		            Node node = nodes.item(i);
		            //
		            Boolean useThisUrl = false;
		            NodeList nodeChilds = node.getChildNodes();
		            for (int j = 0; j < nodeChilds.getLength(); j++) {
		            	Node childNode = nodeChilds.item(j);
		            	String nodeName =  childNode.getNodeName();
		            	String nodeValue = childNode.getTextContent();
		            	tempUrl = "";
						if (nodeName == "recommendedBandwidth")
						{
							Log.v("XML", "Bandwith "+nodeValue);
							if (nodeValue == "DSL3000"){
								useThisUrl = true;
							}
							break;
							
						}else if(nodeName == "fileName"){
							tempUrl = nodeValue;	
		            	}else
						{
							Log.v("XML", "Untreated Nodetype "+nodeName + " with value " + nodeValue);
							
						}
						
		            }
		            if (useThisUrl == true ){
		            	videoPath = tempUrl;
		            }
		           
			    }
		           			 
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	void OpenMovieIntent(){
		if (videoPath != null){
			Intent intent = new Intent(Intent.ACTION_VIEW); 
			intent.setDataAndType(Uri.parse(videoPath), "video/mp4");
			startActivity(intent);
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
