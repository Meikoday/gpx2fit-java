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

    List<Point> points;
}
