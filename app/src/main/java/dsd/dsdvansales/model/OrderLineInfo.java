package dsd.dsdvansales.model;

import java.io.Serializable;

public class OrderLineInfo implements Serializable {
    public int transactionType = 0;
    public String itemNumber = "";
    public int quantity = 0;
    public float price = 0;
    public String strShortDesc = "";
    public String strLongDesc = "";
}
