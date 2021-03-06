package com.wisecarCompany.wisecarapp.user.vehicle;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.wisecarCompany.wisecarapp.function.driverLog.DriverLog;

import java.util.List;
import java.util.Set;

public class Vehicle implements Comparable<Vehicle> {

    private String vehicle_id;
    private String registration_no;
    private String make_name;
    private String model_name;
    private String make_year;
    private String state;
    private String description;
    private String user_id;
    private String user_name;
    private Bitmap image;
    private String state_name;
    private List<Integer> services;
    private Set<DriverLog> logs;

    public Vehicle() {

    }

    public Vehicle(String registration_no, String make_name, String model_name, String make_year, String state, String description, Bitmap image) {
        this.registration_no = registration_no.replaceAll("\r\n|\r|\n", "");
        this.make_name = make_name;
        this.model_name = model_name;
        this.make_year = make_year;
        this.state = state;
        this.description = description;
        this.image = image;
    }

    public Vehicle(String vehicle_id, String registration_no, String make_name, String model_name, String make_year, String description, String user_id, String user_name, Bitmap image, String state_name) {
        this.vehicle_id = vehicle_id;
        this.registration_no = registration_no.replaceAll("\r\n|\r|\n", "");
        this.make_name = make_name;
        this.model_name = model_name;
        this.make_year = make_year;
        this.description = description;
        this.user_id = user_id;
        this.user_name = user_name;
        this.image = image;
        this.state_name = state_name;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public void setRegistration_no(String registration_no) {
        this.registration_no = registration_no.replaceAll("\r\n|\r|\n", "");
    }

    public String getMake_name() {
        return make_name;
    }

    public void setMake_name(String make_name) {
        this.make_name = make_name;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getMake_year() {
        return make_year;
    }

    public void setMake_year(String make_year) {
        this.make_year = make_year;
    }

    public String getDescription() {
        return description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

/*
    public void setImage(byte[] imageByte) {
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        this.image = imageBitmap;
    }

    public void setImage(String imageBase64) {
        byte[] imageByte = Base64.decode(imageBase64, Base64.DEFAULT);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        this.image = imageBitmap;
    }
*/

    public String getState_name() {
        return state_name;
    }

    public void setState_name(String state_name) {
        this.state_name = state_name;
    }

    public List<Integer> getServices() {
        return services;
    }

    public void setServices(List<Integer> services) {
        this.services = services;
    }

    public Set<DriverLog> getLogs() {
        return logs;
    }

    public void setLogs(Set<DriverLog> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicle_id='" + vehicle_id + '\'' +
                ", registration_no='" + registration_no + '\'' +
                ", make_name='" + make_name + '\'' +
                ", model_name='" + model_name + '\'' +
                ", make_year='" + make_year + '\'' +
                ", state='" + state + '\'' +
                ", description='" + description + '\'' +
                ", user_id=" + user_id +
                ", user_name='" + user_name + '\'' +
                ", image=" + image +
                ", state_name='" + state_name + '\'' +
                ", services=" + services +
                ", logs=" + logs +
                '}';
    }

    @Override
    public int compareTo(Vehicle o) {
        try {
            return Integer.parseInt(o.getVehicle_id()) - Integer.parseInt(this.getVehicle_id());
        } catch (Exception e) {
            return o.getVehicle_id().compareTo(this.getVehicle_id());
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Vehicle) {
            return  ((Vehicle) obj).getVehicle_id().equals(this.getVehicle_id());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return Integer.parseInt(getVehicle_id());
        } catch (Exception e) {
            return super.hashCode();
        }
    }
}
