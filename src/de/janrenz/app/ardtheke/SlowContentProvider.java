package de.janrenz.app.ardtheke;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class SlowContentProvider extends ContentProvider {
    
    public static final String TAG = SlowContentProvider.class.getSimpleName();
    
    @Override
    public boolean onCreate() {
        Log.i(TAG,"onCreate");
        return true;
    }
    public String readJSONFeed(String URL) {
    	StringBuilder stringBuilder = new StringBuilder(); HttpClient httpClient = new DefaultHttpClient(); HttpGet httpGet = new HttpGet(URL);
    	try {
    	HttpResponse response = httpClient.execute(httpGet); StatusLine statusLine = response.getStatusLine(); int statusCode = statusLine.getStatusCode();
    	if (statusCode == 200) {
    	HttpEntity entity = response.getEntity(); InputStream inputStream = entity.getContent(); BufferedReader reader = new BufferedReader(
    	new InputStreamReader(inputStream)); String line;
    	while ((line = reader.readLine()) != null) { stringBuilder.append(line);
    	}
    	inputStream.close(); } else {
    		Log.d("readJSONFeed", "Failed to download file"); }
    	} catch (Exception e) {
    		Log.d("readJSONFeed", e.getLocalizedMessage()); }
    	return stringBuilder.toString(); 
    }
	
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
            Log.i(TAG,uri.toString());
            //Thread.sleep(3000);
            String url= "http://m-service.daserste.de/appservice/1.4.1/video/list/1364472896?func=getVideoList&unixTimestamp=1364472896";

            String result = "";
            MatrixCursor cursor = new MatrixCursor(new String[]{"_id","title", "image", "extId"});
            try {
            	result = readJSONFeed(url);
            	// TODO Auto-generated catch block
            	JSONArray jsonArray = new JSONArray(result); 
            	for(int i=0;i<jsonArray.length();i++){
            	
            	
            		JSONObject json_data = jsonArray.getJSONObject(i);
            		//build the Headline
            		String t2 = android.text.Html.fromHtml(json_data.getString("Title2")).toString();
            		String t3 = android.text.Html.fromHtml(json_data.getString("Title3")).toString();
            		cursor.addRow(new Object[]{0,t2 + " " + t3, json_data.getString("ImageUrl").toString() , json_data.getString("VId")});
            	}
		    } catch (JSONException e) {
		    	e.printStackTrace();
		    	return null;
		    }

            Log.i(TAG,"returning " + cursor);
            
  
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;  
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;  
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;  
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;  
    }
}