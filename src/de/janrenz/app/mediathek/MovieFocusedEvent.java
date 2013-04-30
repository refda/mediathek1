package de.janrenz.app.mediathek;

public class MovieFocusedEvent {
  public final int pos;
  public final int dayTimestamp;

  public MovieFocusedEvent(int pos, int dayTimestamp ) {
    this.pos = pos;
    this.dayTimestamp = dayTimestamp;
  }

  @Override public String toString() {
    return new StringBuilder("(") //
        .append(", ") //
        .append(pos) //
        .append(")") //
        .append("td:"+dayTimestamp)
        .toString();
  }
}