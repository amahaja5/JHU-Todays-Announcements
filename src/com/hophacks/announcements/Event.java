package com.hophacks.announcements;

import java.io.Serializable;

public class Event implements Serializable, Comparable<Event> {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    String url, title, des;

    public boolean equals(Event e) {
        return e.des.equals(this.des);
    }

    @Override
    public int compareTo(Event e) {
        // TODO Auto-generated method stub
        return e.des.compareTo(this.des);
    }

    public String toString() {
        return this.title;
    }
}