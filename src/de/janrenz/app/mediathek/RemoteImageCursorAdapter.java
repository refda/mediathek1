package de.janrenz.app.mediathek;

import java.util.Date;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class RemoteImageCursorAdapter  extends SimpleCursorAdapter implements Filterable {
		 
	    private Context context;
	    private int layout;
	 
	    private LayoutInflater mLayoutInflater;
	    
	    public RemoteImageCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to) {
	        super(context, layout, c, from, to);
	        this.context = context;
	        this.layout = layout;
	        this.mLayoutInflater = LayoutInflater.from(context); 
	    }
	 
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	    	  View v = mLayoutInflater.inflate(R.layout.headline_item, parent, false);
	          return v;
	    }
	    
	 
	    
	    @SuppressWarnings("deprecation")
		@Override
	    public void bindView(View v, Context context, Cursor c) {
	    	  String title = c.getString(c.getColumnIndexOrThrow("title"));
	    	  String subtitle = c.getString(c.getColumnIndexOrThrow("subtitle"));
	    	  String imagePath = c.getString(c.getColumnIndexOrThrow("image"));
	    	  String startTime = c.getString(c.getColumnIndexOrThrow("startTime"));
	    	  String startTimeAsTimestamp = c.getString(c.getColumnIndex("startTimeAsTimestamp"));
	          /**
	           * Next set the text of the entry.
	           */
	          
	          TextView title_text = (TextView) v.findViewById(R.id.text_view);
	          if (title_text != null) {
	              title_text.setText(title);
	          }
	          
	          TextView subtitle_text = (TextView) v.findViewById(R.id.text_view_sub);
	          if (subtitle_text != null) {
	        	  subtitle_text.setText(subtitle);
	          }
	          TextView subtitle2_text = (TextView) v.findViewById(R.id.text_view_sub2);
	          if (subtitle2_text != null) {
	        	  Date dt = new Date();
	      		// z.B. 'Fri Jan 26 19:03:56 GMT+01:00 2001'
	        	  dt.setTime(Integer.parseInt(startTimeAsTimestamp)*1000);
	      		dt.setHours(0);
	      		dt.setMinutes(0);
	      		dt.setSeconds(0);
	        	subtitle2_text.setText("ARD > " + startTime + " Uhr");
	          }
	          /**
	           * Set the image
	           */
	          DisplayImageOptions loadingOptions = new DisplayImageOptions.Builder()
	          //.showStubImage(R.drawable.ic_stub)
	          // .showImageForEmptyUri(R.drawable.ic_empty)
	          .showImageOnFail(R.drawable.ic_error)
	          .cacheInMemory()
	          //.cacheOnDisc()
	          .build();
	          ImageView image_view =  (ImageView) v.findViewById(R.id.thumbnail);
	         
	          if (image_view != null) {
	        	  ImageLoader.getInstance().displayImage(imagePath, image_view, loadingOptions);
	          }
	    }
	
}
