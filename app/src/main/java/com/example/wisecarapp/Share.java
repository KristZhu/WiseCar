package com.example.wisecarapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Share {

    private String share_id;
    private boolean recurring;
    private Date recurring_end_date;
    private boolean[] recurring_days;
    private String cust_id;
    private String company_name;
    private String company_id;
    private Date start_time;
    private Date end_time;

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

    public void setRecurring_end_date(String recurring_end_date) throws ParseException {
        this.recurring_end_date = new SimpleDateFormat("yyyy-MM-dd").parse(recurring_end_date);
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

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) throws ParseException {
        this.start_time = new SimpleDateFormat("HH:mm:ss").parse(start_time);
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) throws ParseException {
        this.end_time = new SimpleDateFormat("HH:mm:ss").parse(end_time);
    }
}
