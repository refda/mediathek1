/**
 * This hold our Movies
 */
package de.janrenz.app.ardtheke;

public class Movie {
	
	private String title;
	private String subtitle;
	private String extId;
	private String thumbnail;
	private String duration;	
	private String[][] sources;
	
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
	public String[][] getSources() {
		return sources;
	}
	public void setSources(String[][] sources) {
		this.sources = sources;
	}
}
