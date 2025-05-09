package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class TourInfoResult{
    public int result = 0;
    public String message = "";
    public TourInfo tour = null;
    public List<TourDetail> tour_details = new ArrayList<>();
}
