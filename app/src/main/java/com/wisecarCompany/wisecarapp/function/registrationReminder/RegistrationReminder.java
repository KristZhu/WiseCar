package com.wisecarCompany.wisecarapp.function.registrationReminder;

import java.util.Date;

public class RegistrationReminder {

    private String id;
    private String registrationNo;
    private Date date;
    private String payRef;
    private Date expireDate;
    private boolean sentBefore;

    private String documentLink;
    private String emailAddress;

    public RegistrationReminder(String id, String payRef, Date date, Date expire, String url) {
        this.id = id;
        this.date = date;
        this.payRef = payRef;
        this.expireDate = expire;
        this.documentLink = url;
    }

    public RegistrationReminder(String id, String registrationNo, Date date, String payRef, Date expire, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.date = date;
        this.payRef = payRef;
        this.expireDate = expire;
        this.sentBefore = sentBefore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPayRef() {
        return payRef;
    }

    public void setPayRef(String payRef) {
        this.payRef = payRef;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expire) {
        this.expireDate = expire;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String url) {
        this.documentLink = url;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
