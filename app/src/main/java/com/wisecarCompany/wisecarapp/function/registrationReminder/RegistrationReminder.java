package com.wisecarCompany.wisecarapp.function.registrationReminder;

import java.util.Date;

public class RegistrationReminder {

    private String id;
    private String registrationNo;
    private Date date;
    private String payRef;
    private Date expire;
    private boolean sentBefore;

    private String url;

    public RegistrationReminder(String id, String payRef, Date date, Date expire, String url) {
        this.id = id;
        this.date = date;
        this.payRef = payRef;
        this.expire = expire;
        this.url = url;
    }

    public RegistrationReminder(String id, String registrationNo, Date date, String payRef, Date expire, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.date = date;
        this.payRef = payRef;
        this.expire = expire;
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

    public Date getExpire() {
        return expire;
    }

    public void setExpire(Date expire) {
        this.expire = expire;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
