package com.wisecarCompany.wisecarapp.function.insuranceRecord;

import java.util.Date;

public class InsuranceRecord {

    private String id;
    private String registrationNo;
    private String policyNo;
    private String insurer;
    private Date endDate;
    private boolean sentBefore;

    private Date startDate;
    private String type;
    private String url;

    public InsuranceRecord(String id, String registrationNo, String policyNo, String insurer, Date endDate, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.policyNo = policyNo;
        this.insurer = insurer;
        this.endDate = endDate;
        this.sentBefore = sentBefore;
    }

    public InsuranceRecord(String id, String policyNo, String insurer, Date startDate, Date endDate, String type, String url) {
        this.id = id;
        this.policyNo = policyNo;
        this.insurer = insurer;
        this.endDate = endDate;
        this.startDate = startDate;
        this.type = type;
        this.url = url;
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

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public String getInsurer() {
        return insurer;
    }

    public void setInsurer(String insurer) {
        this.insurer = insurer;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
