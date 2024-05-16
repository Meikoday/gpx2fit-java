package com.yangyang5214.gpx2fit.model;


import com.garmin.fit.DateTime;
import lombok.Setter;
import lombok.Getter;


@Setter
@Getter
public class Point {
    String Lat;
    String Lon;
    String ele;
    DateTime time;
}
