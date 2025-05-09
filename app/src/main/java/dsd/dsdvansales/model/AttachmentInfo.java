package dsd.dsdvansales.model;

public class AttachmentInfo {
    public String fileType = "";
    public String fileName = "";

    public UAttachmentInfo convertToUAttachment(String strBaseUrl, String noteId)
    {
        UAttachmentInfo info = new UAttachmentInfo();
        info.strFileName = fileName;
        info.strFilePath = strBaseUrl + "api/customer-note-attachments/file?fileName=" + fileName + "&noteId=" + noteId;
        info.nTrxnNo = -1;
        return info;
    }
}
