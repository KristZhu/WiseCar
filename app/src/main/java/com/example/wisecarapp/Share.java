package com.example.wisecarapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Share {

    private int share_id;
    private int recurring_flag;
    private Date recurring_end_date;
    private String recurring_days;
    private int cust_id;
    private String company_name;
    private Date start_time;
    private Date end_time;

    public int getShare_id() {
        return share_id;
    }

    public void setShare_id(int share_id) {
        this.share_id = share_id;
    }

    public int getRecurring_flag() {
        return recurring_flag;
    }

    public void setRecurring_flag(String recurring_flag) {
        this.recurring_flag = Integer.parseInt(recurring_flag);
    }

    public Date getRecurring_end_date() {
        return recurring_end_date;
    }

    public void setRecurring_end_date(String recurring_end_date) throws ParseException {
        this.recurring_end_date = new SimpleDateFormat("yyyy-MM-dd").parse(recurring_end_date);
    }

    public String getRecurring_days() {
        return recurring_days;
    }

    public void setRecurring_days(String recurring_days) {
        this.recurring_days = recurring_days;
    }

    public int getCust_id() {
        return cust_id;
    }

    public void setCust_id(int cust_id) {
        this.cust_id = cust_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
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
