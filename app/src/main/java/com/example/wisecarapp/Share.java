package com.example.wisecarapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class Share {

    private boolean isShare = true;
    private String share_id;
    private String cust_id; // company ID
    private String company_name;
    private boolean recurring;
    private Date start_time;
    private Date end_time;
    private Date date;  // start date without time, or simply the date
    private Date recurring_end_date;
    private boolean[] recurring_days;   //[0]: Sunday
    private Map<Integer, Boolean> servicesVisibility = new TreeMap<>();   //value: visibility


    public boolean isShare() {
        return isShare;
    }

    public void setShare(boolean share) {
        isShare = share;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getShare_id() {
        return share_id;
    }

    public void setShare_id(String share_id) {
        this.share_id = share_id;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public Date getRecurring_end_date() {
        return recurring_end_date;
    }

    public void setRecurring_end_date(Date recurring_end_date) {
        this.recurring_end_date = recurring_end_date;
    }

    public boolean[] getRecurring_days() {
        return recurring_days;
    }

    public void setRecurring_days(boolean[] recurring_days) {
        this.recurring_days = recurring_days;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

//    public String getCompany_id() {
//        return company_id;
//    }
//
//    public void setCompany_id(String company_id) {
//        this.company_id = company_id;
//    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public Map<Integer, Boolean> getServicesVisibility() {
        return servicesVisibility;
    }

    public void setServicesVisibility(Map<Integer, Boolean> servicesVisibility) {
        this.servicesVisibility = servicesVisibility;
    }
}
