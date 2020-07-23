package com.wisecarCompany.wisecarapp.function.serviceRecords;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ServiceRecord implements Comparable<ServiceRecord>, Serializable {

    private String id;

    private String registrationNo;
    private Date date;
    private String refNo;
    private Date nextDate;
    private double nextDistance;
    private boolean sentBefore;

    private String centre;
    private List<String> options;
    private String notes;
    private String documentLink;

    private String emailAddress;

    public ServiceRecord() {

    }

    public ServiceRecord(String registrationNo, Date date, String refNo, Date nextDate, double nextDistance, boolean sentBefore) {
        this.registrationNo = registrationNo;
        this.date = date;
        this.refNo = refNo;
        this.nextDate = nextDate;
        this.nextDistance = nextDistance;
        this.sentBefore = sentBefore;
    }

    public ServiceRecord(String id, String registrationNo, Date date, String refNo, Date nextDate, double nextDistance, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.date = date;
        this.refNo = refNo;
        this.nextDate = nextDate;
        this.nextDistance = nextDistance;
        this.sentBefore = sentBefore;
    }

    public ServiceRecord(Date date, String centre, String refNo, List<String> options, String notes, Date nextDate, double nextDistance, String documentLink) {
        this.date = date;
        this.centre = centre;
        this.refNo = refNo;
        this.options = options;
        this.notes = notes;
        this.nextDate = nextDate;
        this.nextDistance = nextDistance;
        this.documentLink = documentLink;
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

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public Date getNextDate() {
        return nextDate;
    }

    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    public double getNextDistance() {
        return nextDistance;
    }

    public void setNextDistance(double nextDistance) {
        this.nextDistance = nextDistance;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public String getCentre() {
        return centre;
    }

    public void setCentre(String centre) {
        this.centre = centre;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getOptionsStr() {
        if (options == null || options.size() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String option : options) {
            sb.append(option).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        return "ServiceRecord{" +
                "registrationNo='" + registrationNo + '\'' +
                ", date=" + date +
                ", refNo='" + refNo + '\'' +
                ", nextDate=" + nextDate +
                ", nextDistance=" + nextDistance +
                ", sentBefore=" + sentBefore +
                ", centre='" + centre + '\'' +
                ", options=" + options +
                ", notes='" + notes + '\'' +
                ", documentLink='" + documentLink + '\'' +
                '}';
    }

    @Override
    public int compareTo(ServiceRecord o) {
        return this.getRegistrationNo().compareTo(o.getRegistrationNo()) != 0 ?
                this.getRegistrationNo().compareTo(o.getRegistrationNo()) :
                this.getDate().compareTo(o.getDate());
    }

}
