package de.janrenz.app.mediathek;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
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

public class ArdMediathekProvider extends ContentProvider {

	public static final String TAG = ArdMediathekProvider.class.getSimpleName();

	@Override
	public boolean onCreate() {
		
		return true;
	}
	

	public String readJSONFeed(String URL) {
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
				Log.e("readJSONFeed", "Failed to download file");
				return "";
			}
		} catch (Exception e) {
			Log.e("readJSONFeed", e.getLocalizedMessage());
			return "";
		}
		return stringBuilder.toString();
	}

	@Override
	public Cursor query(Uri uri, String[] strings, String s, String[] strings1,
			String s1) {

		String queryparam = uri.getQueryParameter("timestamp");
		if (queryparam == null) {
			queryparam = "0";
		}
		String queryparamReload = uri.getQueryParameter("reload");
		String queryExtReload = "";
		if (queryparamReload != null) {
			queryExtReload = "&reload=" + Math.random();
		}
		Integer timestamp = Integer.parseInt(queryparam);
	
		// String url=
		// "http://m-service.daserste.de/appservice/1.4.1/video/list/" + curtime
		// + "?func=getBroadcastList&unixTimestamp=" + curtime;

		String url = "http://m-service.daserste.de/appservice/1.4.1/video/list/"
				+ timestamp + "?func=getVideoList&unixTimestamp=" + timestamp;

		String result = "";
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title",
				"subtitle", "image", "extId", "startTime", "startTimeAsTimestamp", "isLive" });
		result = readJSONFeed(url);
		try {
			if (result == "") {
				return cursor;
			}
			// TODO Auto-generated catch block
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject json_data = jsonArray.getJSONObject(i);
				// build the Headline
				String t2 = android.text.Html.fromHtml(
						json_data.getString("Title3")).toString();
				String t3 = android.text.Html.fromHtml(
						json_data.getString("Title2")).toString();

				// Handle grouped views, like tatort
				if (json_data.getBoolean("IsGrouped")) {
					String mtime = json_data.getString("BTime").toString();
					String cliplisturl = "http://m-service.daserste.de/appservice/1.4.1/video/clip/list/"
							+ mtime
							+ "/"
							+ URLEncoder.encode(t3)
							+ "?func=getVideoClipList&clipTimestamp="
							+ mtime
							+ "&clipTitle=" + URLEncoder.encode(t3);
					String result2 = "";
					result2 = readJSONFeed(cliplisturl);
					JSONArray jsonArray2 = new JSONArray(result2);
					for (int j = 0; j < jsonArray2.length(); j++) {
						JSONObject json_data2 = jsonArray2.getJSONObject(j);
						t2 = android.text.Html.fromHtml(
								json_data2.getString("Title3")).toString();
						t3 = android.text.Html.fromHtml(
								json_data2.getString("Title2")).toString();

						// only add movie if it has a video
						if (android.text.Html.fromHtml(
								json_data2.getString("VId")).toString() != "") {

							cursor.addRow(new Object[] {
									1000 + j,
									t2,
									t3,
									json_data2.getString("ImageUrl").toString(),
									json_data2.getString("VId"),
									json_data2.getString("BTimeF").toString(),
									json_data2.getString("BTime").toString(),
									json_data2.getString("IsLive")
									});
						}
					}
				}
				if (!json_data.getBoolean("IsGrouped")) {
					if (android.text.Html.fromHtml(json_data.getString("VId"))
							.toString() != "") {
						cursor.addRow(new Object[] { i, t2, t3,
								json_data.getString("ImageUrl").toString(),
								json_data.getString("VId"),
								json_data.getString("BTimeF").toString(),
								json_data.getString("BTime").toString(),
								json_data.getString("IsLive")});
					}
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
	public int update(Uri uri, ContentValues contentValues, String s,
			String[] strings) {
		return 0;
	}
}