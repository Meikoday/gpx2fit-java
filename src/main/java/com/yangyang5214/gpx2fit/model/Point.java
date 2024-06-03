package com.yangyang5214.gpx2fit.model;


import com.garmin.fit.DateTime;
import lombok.Setter;
import lombok.Getter;


@Setter
@Getter
public class Point {
    Double lat;
    Double lon;
    Float ele;
    Float distance;
    DateTime time;
    Float speed;

    short hr;
    short cadence;

    //地球平均半径，单位：公里
    private static final float EARTH_RADIUS = 6371.00F * 1000;

    private static double toRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    public float calculateDistance(Point p) {
        double lat1 = lat;
        double lon1 = lon;
        double alt1 = ele;
        double lat2 = p.getLat();
        double lon2 = p.getLon();
        double alt2 = p.getEle();

        double deltaLat = toRadians(lat2 - lat1);
        double deltaLon = toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double horizontalDistance = EARTH_RADIUS * c;

        double deltaAlt = alt2 - alt1;

        double distance = Math.sqrt(horizontalDistance * horizontalDistance + deltaAlt * deltaAlt);

        return (float) distance;
    }


    public Long subTs(Point p) {
        return time.getTimestamp() - p.time.getTimestamp();
    }

    public float subEle(Point p) {
        return p.ele - ele;
    }
}
