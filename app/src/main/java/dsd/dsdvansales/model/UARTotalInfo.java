package dsd.dsdvansales.model;

import java.util.*;

public class UARTotalInfo {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public int nChainNo = 0;
	public int nCustNo = 0;
	public String strTrxnDate = "";
	public String strTrxnTime = "";
	public float nTotalDue = 0;
	public float nTotalUnalloc = 0;
	public float nTotalCash = 0;
	public float nTotalCheque = 0;
	public long nInvoiceTrxnNo = 0;
	public float nInvoiceAmount = 0;
	public boolean bIsVisitedDelivery = false;
	public boolean bIsVisitedPickup = false;
	public long nDeliveryTrxnNo = 0;
	public long nPickupTrxnNo = 0;
	public String strDocNo = "0";
	public String strDocType = "PAY";
	public String strReference = "";
	public List<UARPaymentInfo> paymentList = new ArrayList<>();
}
