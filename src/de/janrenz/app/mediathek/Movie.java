/**
 * This hold our Movies
 */
package de.janrenz.app.mediathek;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
	
	private String title;
	private String subtitle;
	private String extId;
	private String thumbnail;
	private String duration;	
	private String starttime;
	private int starttimestamp;
	private ArrayList<String[]> sources = new ArrayList<String[]>() ;
	
	 public Movie(Parcel in) {  
	     readFromParcel(in);  
	    }  
	 public Movie() {  
	     return;
	    }  
	 private void readFromParcel(Parcel in) {    
	        // ...  
	        title = in.readString();  
	        subtitle = in.readString();  
	        extId = in.readString();  
	        thumbnail = in.readString();  
	        duration = in.readString();  
	        starttime = in.readString();  
	        starttimestamp = in.readInt();  
	        //this will be treated sligty differnet
	        in.readList (sources, String.class.getClassLoader());

	    }  
	  
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {  
    
        public Movie createFromParcel(Parcel in) {  
            return new Movie(in);  
        }  
        
        public Movie[] newArray(int size) {  
            return new Movie[size];  
        }  
        
    };
			
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getExtId() {
		return extId;
	}
	public void setExtId(String extId) {
		this.extId = extId;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	//
	public String getSenderinfo(){
		return "ARD > " + this.getStarttime() + " Uhr";
	}
	public ArrayList<String[]> getSources() {
		return sources;
	}
	public void setSources(ArrayList<String[]> sources) {
		this.sources = sources;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(title);  
		dest.writeString(subtitle); 
		dest.writeString(extId);
		dest.writeString(duration);
		dest.writeString(thumbnail);
		dest.writeString(getStarttime());
		dest.writeInt(getStarttimestamp());
		dest.writeList(sources);
	}
	public int getStarttimestamp() {
		return starttimestamp;
	}
	public void setStarttimestamp(int i) {
		this.starttimestamp = i;
	}
}
