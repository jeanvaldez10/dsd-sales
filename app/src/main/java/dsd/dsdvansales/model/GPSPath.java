package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class GPSPath {
    public long distance = 0; //meter
    public String duration = ""; //seconds
    public GPSPosition start_location = new GPSPosition();
    public GPSPosition end_location = new GPSPosition();
    public List<GPSPath> pathList = new ArrayList<>();
    public static class GPSPosition{
        public double lat = 0;
        public double lng = 0;
    }
}
