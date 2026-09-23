package com.example.android_assign;

final class LocationRecord {
    final double latitude;
    final double longitude;
    final String time;
    String address;

    LocationRecord(double latitude, double longitude, String address, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.time = time;
    }
}
