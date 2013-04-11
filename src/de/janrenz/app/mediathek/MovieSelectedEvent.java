package de.janrenz.app.mediathek;
import java.util.ArrayList;

import de.janrenz.app.mediathek.Movie;;
public class MovieSelectedEvent {
  public final int pos;
  public final String extId;
  public final ArrayList<Movie> mList;

  public MovieSelectedEvent(int pos, String extId, ArrayList<Movie> mList ) {
    this.pos = pos;
    this.extId = extId;
    this.mList = mList;
  }

  @Override public String toString() {
    return new StringBuilder("(") //
        .append(", ") //
        .append(extId) //
        .append(")") //
        .toString();
  }
}