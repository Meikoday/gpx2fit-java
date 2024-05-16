package com.yangyang5214.gpx2fit.model;


import com.garmin.fit.DateTime;
import lombok.Setter;
import lombok.Getter;


@Setter
@Getter
public class Point {
    Double Lat;
    Double Lon;
    Float ele;
    Float distance;
    DateTime time;


    //地球平均半径，单位：公里
    private static final float EARTH_RADIUS = 6371.00F * 1000;

    /**
     * 将角度转换为弧度
     *
     * @param degrees
     * @return
     */
    private static double toRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    /**
     * 计算两个点之间的距离
     *
     * @param p
     * @return
     */
    public float calculateDistance(Point p) {
        double lat1 = Lat;
        double lon1 = Lon;

        double lat2 = p.getLat();
        double lon2 = p.getLon();

        double deltaLat = toRadians(lat2 - lat1);
        double deltaLon = toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

        return (float) (EARTH_RADIUS * c);
    }
}
