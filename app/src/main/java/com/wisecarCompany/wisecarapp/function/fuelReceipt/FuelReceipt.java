package com.wisecarCompany.wisecarapp.function.fuelReceipt;

import java.util.Date;

public class FuelReceipt {

    private String id;
    private String registrationNo;
    private Date date;
    private String invoiceRef;
    private String companyName;
    private boolean sentBefore;

    private String type;
    private double fuelAmount;
    private double paidAmount;
    private String documentLink;
    private String emailAddress;

    public FuelReceipt(String id, String registrationNo, Date date, String ref, String companyName, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo.replaceAll("\r\n|\r|\n", "");
        this.date = date;
        this.invoiceRef = ref;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.sentBefore = sentBefore;
    }

    public FuelReceipt(String id, String ref, Date date, String type, double fuelAmount, double paidAmount, String companyName, String url) {
        this.id = id;
        this.date = date;
        this.invoiceRef = ref;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.type = type;
        this.fuelAmount = fuelAmount;
        this.paidAmount = paidAmount;
        this.documentLink = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationNo() {
        return registrationNo.replaceAll("\r\n|\r|\n", "");
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo.replaceAll("\r\n|\r|\n", "");
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getInvoiceRef() {
        return invoiceRef;
    }

    public void setInvoiceRef(String ref) {
        this.invoiceRef = ref;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFuelAmount() {
        return fuelAmount;
    }

    public void setFuelAmount(double fuelAmount) {
        this.fuelAmount = fuelAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return "FuelReceipt{" +
                "id='" + id + '\'' +
                ", registrationNo='" + registrationNo + '\'' +
                ", date=" + date +
                ", invoiceRef='" + invoiceRef + '\'' +
                ", companyName='" + companyName + '\'' +
                ", sentBefore=" + sentBefore +
                ", type='" + type + '\'' +
                ", fuelAmount=" + fuelAmount +
                ", paidAmount=" + paidAmount +
                ", documentLink='" + documentLink + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                '}';
    }
}
