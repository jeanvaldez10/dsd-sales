package dsd.dsdvansales.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderInfo implements Serializable {
    public int chainNumber = 0;
    public int customerNumber = 0;
    public String orderType = "";
    public int orderNumber = 0;
    public List<OrderLineInfo> orderLines = new ArrayList<>();
    public String purchaseOrderNumber = "";
    public String deliveryInstructions = "";
    public String requestedDeliveryDate = "";
    public float totalQuantityOrdered = 0.0f;
}
