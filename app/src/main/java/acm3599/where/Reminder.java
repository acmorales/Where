package acm3599.where;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Andrew on 11/11/2016.
 */

public class Reminder {

    private String title;
    private String content;
    private boolean active;
    private boolean hasLocation;
    private Place place;

    public Reminder(String t, String c, Place p) {
        title = t;
        content = c;
        place = p;
        active = true;
        if(place == null) {
            hasLocation = false;
        } else {
            hasLocation = true;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        if(content != null) {
            return content;
        } else {
            return "";
        }
    }

    public String getLocName() {
        return place.getName().toString();
    }

    public String getAddress() {
        if(hasLocation) {
            return place.getAddress().toString();
        } else {
            return "";
        }
    }

    public LatLng getLatLng() {
        return place.getLatLng();
    }

    public boolean hasLocation() {
        return hasLocation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean b) {
        active = b;
    }

    public boolean equals(Reminder r) {
        return this.getAddress().equals(r.getAddress());
    }

    @Override
    public String toString() {
        return "title: '" + title + "'\tcontent: '" + content + "'\taddress: '" + getAddress() + "'";
    }
}
