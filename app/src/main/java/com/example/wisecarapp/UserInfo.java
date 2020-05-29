package com.example.wisecarapp;

import java.util.List;

class UserInfo {
    private static String username;
    private static String userEmail;
    private static List<Vehicle> vehicles;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        UserInfo.username = username;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        UserInfo.userEmail = userEmail;
    }

    public static List<Vehicle> getVehicles() {
        return vehicles;
    }

    public static void setVehicles(List<Vehicle> vehicles) {
        UserInfo.vehicles = vehicles;
    }

    public static boolean addVehicle(Vehicle vehicle) {
        return vehicles.add(vehicle);
    }

    /*
    public static boolean deleteVehicle() {

    }

    public Vehicle getVehicle() {

    }

    

     */
}
