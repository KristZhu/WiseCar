package com.wisecarCompany.wisecarapp.user.licence;

import android.graphics.Bitmap;

import java.util.Date;

public class Licence {

    private boolean active;
    private String number;
    private String type;
    private Date startDate;
    private Date expiryDate;
    private boolean remind;
    private Bitmap licenceImg;
    private String expire;



    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRemind() {
        return remind;
    }

    public void setRemind(boolean remind) {
        this.remind = remind;
    }

    public Bitmap getLicenceImg() {
        return licenceImg;
    }

    public void setLicenceImg(Bitmap licenceImg) {
        this.licenceImg = licenceImg;
    }
}
