package com.yangyang5214.gpx2fit.model;

import com.garmin.fit.DateTime;
import com.garmin.fit.Sport;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Session {
    Sport sport;
    DateTime startTime;
    DateTime endTime;

    float totalMovingTime;   //移动时间
    float totalElapsedTime; //总时间
    float totalDistance;

    float totalAscent; //总爬升
    float totalDescent; //总下降

    List<Point> points;
}
