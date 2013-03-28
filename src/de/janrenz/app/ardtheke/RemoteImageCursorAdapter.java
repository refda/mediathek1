package de.janrenz.app.ardtheke;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
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
	 
	    @Override
	    public void bindView(View v, Context context, Cursor c) {
	    	  String title = c.getString(c.getColumnIndexOrThrow("title"));
	          String imagePath = c.getString(c.getColumnIndexOrThrow("image"));

	          /**
	           * Next set the title of the entry.
	           */

	          TextView title_text = (TextView) v.findViewById(R.id.text_view);
	          if (title_text != null) {
	              title_text.setText(title);
	          }
	          /**
	           * Set the image
	           */
	          SmartImageView image_view = (SmartImageView) v.findViewById(R.id.thumbnail);
	          if (image_view != null) {
	        	  image_view.setImageUrl(imagePath);
	          }
	    }
	 /**
	    @Override
	    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
	        if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }
	 
	        StringBuilder buffer = null;
	        String[] args = null;
	        if (constraint != null) {
	            buffer = new StringBuilder();
	            buffer.append("UPPER(");
	            buffer.append(People.NAME);
	            buffer.append(") GLOB ?");
	            args = new String[] { constraint.toString().toUpperCase() + "*" };
	        }
	 
	        return context.getContentResolver().query(People.CONTENT_URI, null,
	                buffer == null ? null : buffer.toString(), args, People.NAME + " ASC");
	    */
}
