package com.yangyang5214.gpx2fit;


import com.garmin.fit.*;
import com.yangyang5214.gpx2fit.gpx.GpxParser;
import com.yangyang5214.gpx2fit.model.Point;
import com.yangyang5214.gpx2fit.model.Session;

import java.time.ZoneId;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.yangyang5214.gpx2fit.examples.EncodeActivity.CreateActivityFile;


public class App {

    static int degree = 11930465;

    public static void main(String[] args) {
        int argsLen = args.length;
        if (argsLen == 0) {
            System.out.println("java -jar gpx2fit.jar xxx.gpx");
            return;
        }
        String gpxFile = args[0];
        System.out.format("Start parser gpx %s\n", gpxFile);
        GpxParser gpxParser = new GpxParser(gpxFile);
        Session session = gpxParser.parser();
        if (session == null) {
            return;
        }
        System.out.format("Find point size %d\n", session.getPoints().size());
        System.out.format(" - sport is %s\n", session.getSport().name());
        System.out.format(" - distance %.2f km\n", session.getTotalDistance() / 1000);
        System.out.format(" - totalMovingTime %.2f s\n", session.getTotalMovingTime());
        System.out.format(" - totalElapsedTime %.2f s\n", session.getTotalElapsedTime());
        System.out.format(" - totalAscent %d \n", session.getTotalAscent());
        System.out.format(" - totalDescent %d \n", session.getTotalDescent());

        String pathname = "result.fit";
        if (args.length == 2) {
            pathname = args[1];
        }
        System.out.format("Save file to %s\n", pathname);
        CreateActivity(session, pathname);
    }

    public static void CreateActivity(Session session, String resultPath) {
        List<Point> points = session.getPoints();

        DateTime startTime = session.getStartTime();
        DateTime endTime = session.getEndTime();


        List<Mesg> messages = new ArrayList<Mesg>();

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        EventMesg eventMesg = new EventMesg();
        eventMesg.setTimestamp(startTime);
        eventMesg.setEvent(Event.TIMER); //计时器事件
        eventMesg.setEventType(EventType.START);
        messages.add(eventMesg);

        // Create the Developer Id message for the developer data fields.
        DeveloperDataIdMesg developerIdMesg = new DeveloperDataIdMesg();
        // It is a BEST PRACTICE to reuse the same Guid for all FIT files created by your platform
        byte[] appId = "gpxtgpxtgpxtgpxt".getBytes();
        for (int i = 0; i < appId.length; i++) {
            developerIdMesg.setApplicationId(i, appId[i]);
        }
        developerIdMesg.setDeveloperDataIndex((short) 0);
        developerIdMesg.setApplicationVersion((long) (1.0 * 100));
        messages.add(developerIdMesg);

        for (Point point : points) {
            RecordMesg recordMesg = new RecordMesg();
            recordMesg.setTimestamp(point.getTime());
            recordMesg.setPositionLat((int) (degree * (point.getLat())));
            recordMesg.setPositionLong((int) (degree * (point.getLon())));
            if (point.getEle() != null) {
                recordMesg.setAltitude(point.getEle());
            }
            recordMesg.setDistance((point.getDistance()));
//            recordMesg.setSpeed((float) 1);
//            recordMesg.setHeartRate((short) ((Math.sin(twoPI * (0.01 * i + 10)) + 1.0) * 127.0)); // Sine
//            recordMesg.setCadence((short) (i % 255)); // Sawtooth
//            recordMesg.setPower(((short) (i % 255) < 157 ? 150 : 250)); //Square

            messages.add(recordMesg);
        }

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        EventMesg eventMesgStop = new EventMesg();
        eventMesgStop.setTimestamp(endTime);
        eventMesgStop.setEvent(Event.TIMER);
        eventMesgStop.setEventType(EventType.STOP_ALL);
        messages.add(eventMesgStop);

        // Every FIT ACTIVITY file MUST contain at least one Lap message
        LapMesg lapMesg = new LapMesg();
        lapMesg.setMessageIndex(0);
        lapMesg.setTimestamp(endTime);
        lapMesg.setStartTime(startTime);
        lapMesg.setTotalElapsedTime(session.getTotalElapsedTime());
        lapMesg.setTotalMovingTime(session.getTotalMovingTime());
        lapMesg.setTotalTimerTime(session.getTotalMovingTime());
        messages.add(lapMesg);

        // Every FIT ACTIVITY file MUST contain at least one Session message
        SessionMesg sessionMesg = new SessionMesg();
        sessionMesg.setMessageIndex(0);
        sessionMesg.setTimestamp(startTime);
        sessionMesg.setStartTime(startTime);
        sessionMesg.setTotalDistance(session.getTotalDistance());

        sessionMesg.setTotalElapsedTime((session.getTotalElapsedTime()));
        sessionMesg.setTotalMovingTime((session.getTotalMovingTime()));
        sessionMesg.setTotalTimerTime((session.getTotalMovingTime()));

        sessionMesg.setSport(session.getSport());
        sessionMesg.setSubSport(SubSport.GENERIC);
        sessionMesg.setFirstLapIndex(0);
        sessionMesg.setNumLaps(1);

        sessionMesg.setTotalAscent(session.getTotalAscent());
        sessionMesg.setTotalDescent(session.getTotalDescent());

        messages.add(sessionMesg);

        // Every FIT ACTIVITY file MUST contain EXACTLY one Activity message
        ActivityMesg activityMesg = new ActivityMesg();
        activityMesg.setTimestamp(endTime);
        activityMesg.setNumSessions(1);

        ZoneId localZoneId = ZoneId.systemDefault();
        Instant currentTimestamp = Instant.now();
        ZonedDateTime localDateTime = currentTimestamp.atZone(localZoneId);

        long totalTime = endTime.getTimestamp() + localDateTime.getOffset().getTotalSeconds();
        activityMesg.setLocalTimestamp(totalTime);
        activityMesg.setTotalTimerTime(session.getTotalMovingTime());
        messages.add(activityMesg);

        CreateActivityFile(messages, resultPath, startTime);
    }
}
