package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class GooglePath {

    public class Leg
    {
        public Distance distance;
        public Duration duration;
        public Location end_location;
        public Location start_location;
        public List<Leg> steps = new ArrayList<>();
    }

    public class Distance
    {
        public String text;
        public long value;
    }

    public class Duration
    {
        public String text;
        public long value;
    }

    public class Location{
        public double lat;
        public double lng;
    }

    public class Route{
        public List<Leg> legs = new ArrayList<>();
    }

    public List<Route> routes = new ArrayList<>();
}
