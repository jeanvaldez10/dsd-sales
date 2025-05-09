package dsd.dsdvansales.model;

import java.util.List;

/**
 * Created by silve on 10/24/2016.
 */

public class EWayTransactionResponse {
    public class Transaction
    {
        public String AuthorisationCode;
        public String ResponseCode;
        public String ResponseMessage;
        public String InvoiceNumber;
        public String TransactionID;
        public String TransactionStatus;
    }
    public List<Transaction> Transactions;
    public String Errors;
}
