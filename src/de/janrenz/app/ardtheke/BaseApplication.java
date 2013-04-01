package de.janrenz.app.ardtheke;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoaderConfiguration loadingOptions = new ImageLoaderConfiguration.Builder(getApplicationContext())
           .build();
        // Create global configuration and initialize ImageLoader with this configuration

        ImageLoader.getInstance().init(loadingOptions);
    }
}