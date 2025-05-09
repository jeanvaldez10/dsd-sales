package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class LoadHead {
    public String LoadDate = "";
    public int DayNo = 0;
    public String Type = "";
    public String Description = "";
    public int PeriodNo = 0;
    public String DetailFile = "";
    public List<LoadDetl> LoadDetl = new ArrayList<>();
    public int RepNo = 0;
}
