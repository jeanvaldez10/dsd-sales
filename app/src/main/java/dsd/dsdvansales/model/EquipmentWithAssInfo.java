package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silve on 8/9/2017.
 */

public class EquipmentWithAssInfo extends EquipAssInfo{
    public String strSerialNo = "";
    public String strMake = "";
    public String strModel = "";
    public String strDescription = "";
    public String strAltEquipment = "";
    public String strEquipmentType = "";
    public String strEquipmentActType = "";
    public String strApplication = "";
    public String strStatusCode = "";
    public String strAssetType = "";
    public String strNewPlacementDocPath = "";
    public String strRepairedDate = "";
    public String strVerifiedDate = "";
    public String strResponse = "";

    public List<String> photoList = new ArrayList<>();

    public EquipmentWithAssInfo duplicate()
    {
        EquipmentWithAssInfo info = new EquipmentWithAssInfo();
        info.strSerialNo = strSerialNo;
        info.strMake = strMake;
        info.strModel = strModel;
        info.strDescription = strDescription;
        info.strAltEquipment = strAltEquipment;
        info.strEquipmentType = strEquipmentType;
        info.strEquipmentActType = strEquipmentActType;
        info.strApplication = strApplication;
        info.strStatusCode = strStatusCode;
        info.strAssetType = strAssetType;
        info.strNewPlacementDocPath = strNewPlacementDocPath;

        info.nChainNo  = nChainNo;
        info.nCustNo  = nCustNo;
        info.strEquipmentNo  = strEquipmentNo;
        info.strInstalled  = strInstalled;
        info.strAgreementNo  = strAgreementNo;
        info.strAgreementDate  = strAgreementDate;
        info.strBillType  = strBillType;
        info.strBillFreq  = strBillFreq;
        info.strVerifyFlag  = strVerifyFlag;
        info.strVerified   = strVerified;
        info.strFeatures    = strFeatures;
        info.strRepairReason    = strRepairReason;
        info.strRepairNotes    = strRepairNotes;
        info.strRepairImage    = strRepairImage;
        info.nPayRentAmt     = nPayRentAmt;
        info.strOldEquipmentType     = strOldEquipmentType;
        info.strStatus     = strStatus;
        info.strRepairedDate = strRepairedDate;
        info.strVerifiedDate = strVerifiedDate;

        return info;

    }
}
