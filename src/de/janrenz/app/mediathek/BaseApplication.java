package de.janrenz.app.mediathek;

import java.io.File;
import java.io.IOException;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.http.HttpResponseCache;
import android.util.Log;

public class BaseApplication extends Application {
   
    @SuppressLint("NewApi") 
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoaderConfiguration loadingOptions = new ImageLoaderConfiguration.Builder(getApplicationContext())
           .build();
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoader.getInstance().init(loadingOptions);
        try {
        	if (Integer.valueOf(android.os.Build.VERSION.SDK)>13){
        		File httpCacheDir = new File(getApplicationContext().getCacheDir(), "http");
        		long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
        		HttpResponseCache.install(httpCacheDir, httpCacheSize);
        		
        	}
         }
         catch (IOException e) {
            Log.i("APP WIDE", "HTTP response cache installation failed:" + e);
        
        }
    }
}