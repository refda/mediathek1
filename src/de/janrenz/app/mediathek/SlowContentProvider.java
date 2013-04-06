package de.janrenz.app.mediathek;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;

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
    		Log.d("readJSONFeed", "Failed to download file");
    		return "";
    		}
    	} catch (Exception e) {
    		Log.d("readJSONFeed", e.getLocalizedMessage()); 
    		return "";	
    	}
    	return stringBuilder.toString(); 
    }
	
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
         
    	Log.d("CONTENTPROVIDER", uri.toString());
            String queryparam = uri.getQueryParameter("offset");
            if (queryparam == null){
            	queryparam = "0";
            }
            String queryparamReload = uri.getQueryParameter("reload");
            String queryExtReload = "";
            if (queryparamReload != null){
            	queryExtReload = "&reload=" + Math.random();
            }
            Integer offset = Integer.parseInt(queryparam);
            Date dt = new Date();
              // z.B. 'Fri Jan 26 19:03:56 GMT+01:00 2001'
            dt.setHours(0);
            dt.setMinutes(0);
            dt.setSeconds(0);
            
            Long curtime = dt.getTime()/1000 - ((24*60*60)*offset);
            String url= "http://m-service.daserste.de/appservice/1.4.1/video/list/" + curtime + "?func=getVideoList&unixTimestamp=" + curtime;
          
            String result = "";
            MatrixCursor cursor = new MatrixCursor(new String[]{"_id","title", "subtitle", "image", "extId"});
            try {
            	result = readJSONFeed(url);
            	if (result == ""){
            		return cursor;
            	}
            	// TODO Auto-generated catch block
            	JSONArray jsonArray = new JSONArray(result); 
            	for(int i=0;i<jsonArray.length();i++){
            		
            		JSONObject json_data = jsonArray.getJSONObject(i);
            		//build the Headline
            		String t2 = Math.random() +android.text.Html.fromHtml(json_data.getString("Title3")).toString();
            		String t3 = android.text.Html.fromHtml(json_data.getString("Title2")).toString();
            		//Handle grouped views, like tatort
            		if (json_data.getBoolean("IsGrouped")){
            			String mtime = json_data.getString("BTime").toString();
            			String  cliplisturl = "http://m-service.daserste.de/appservice/1.4.1/video/clip/list/" + mtime + "/"+URLEncoder.encode(t3)+"?func=getVideoClipList&clipTimestamp=" + mtime + 
            					"&clipTitle=" + URLEncoder.encode(t3);
            			Log.e("URL" ,cliplisturl );
            			String result2 = "";
            			result2 = readJSONFeed(cliplisturl);
            			JSONArray jsonArray2 = new JSONArray(result2); 
            			Log.e("XML" ,result2);
                    	for(int j=0;j<jsonArray2.length();j++){
                    		JSONObject json_data2 = jsonArray2.getJSONObject(j);
                    		 t2 = android.text.Html.fromHtml(json_data2.getString("Title3")).toString();
                    		 t3 = android.text.Html.fromHtml(json_data2.getString("Title2")).toString();
                    		cursor.addRow(new Object[]{1000+j,t2,t3,json_data2.getString("ImageUrl").toString() , json_data2.getString("VId")});
                    	}
            		} 
            		if (!json_data.getBoolean("IsGrouped") && !json_data.getBoolean("IsLive")){
            			
            			cursor.addRow(new Object[]{i,t2,t3,json_data.getString("ImageUrl").toString() , json_data.getString("VId")});
            		}
            	}
		    } catch (JSONException e) {
		    	e.printStackTrace();
		    	return cursor;
		    }
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