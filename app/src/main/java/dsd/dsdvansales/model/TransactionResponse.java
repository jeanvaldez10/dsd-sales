package dsd.dsdvansales.model;

public class TransactionResponse {
    private String Errors;
    private String AuthorisationCode;
    private String ResponseCode;
    private String ResponseMessage;
    private String TransactionID;
    private boolean TransactionStatus;
    private String TransactionType;

    public String getErrors() {
        return Errors;
    }

    public void setErrors(String errors) {
        Errors = errors;
    }

    public String getAuthorisationCode() {
        return AuthorisationCode;
    }

    public void setAuthorisationCode(String authorisationCode) {
        AuthorisationCode = authorisationCode;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String responseCode) {
        ResponseCode = responseCode;
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }

    public String getTransactionID() {
        return TransactionID;
    }

    public void setTransactionID(String transactionID) {
        TransactionID = transactionID;
    }

    public boolean isTransactionStatus() {
        return TransactionStatus;
    }

    public void setTransactionStatus(boolean transactionStatus) {
        TransactionStatus = transactionStatus;
    }

    public String getTransactionType() {
        return TransactionType;
    }

    public void setTransactionType(String transactionType) {
        TransactionType = transactionType;
    }
}

