package com.yangyang5214;


import com.garmin.fit.*;
import com.yangyang5214.gpx2fit.gpx.GpxParser;
import com.yangyang5214.gpx2fit.model.Point;
import com.yangyang5214.gpx2fit.model.Session;

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
        CreateActivity(session);
    }

    public static void CreateActivity(Session session) {
        final String filename = "result.fit";

        List<Point> points = session.getPoints();

        DateTime startTime = session.getStartTime();
        DateTime endTime = session.getEndTime();


        List<Mesg> messages = new ArrayList<Mesg>();

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        EventMesg eventMesg = new EventMesg();
        eventMesg.setTimestamp(startTime);
        eventMesg.setEvent(Event.TIMER);
        eventMesg.setEventType(EventType.START);
        messages.add(eventMesg);

        // Create the Developer Id message for the developer data fields.
        DeveloperDataIdMesg developerIdMesg = new DeveloperDataIdMesg();
        // It is a BEST PRACTICE to reuse the same Guid for all FIT files created by your platform
        byte[] appId = new byte[]{
                0x1, 0x1, 0x2, 0x3,
                0x5, 0x8, 0xD, 0x15,
                0x22, 0x37, 0x59, (byte) 0x90,
                (byte) 0xE9, 0x79, 0x62, (byte) 0xDB
        };

        for (int i = 0; i < appId.length; i++) {
            developerIdMesg.setApplicationId(i, appId[i]);
        }
        developerIdMesg.setDeveloperDataIndex((short) 0);
        messages.add(developerIdMesg);

        // Create the Developer Data Field Descriptions
        FieldDescriptionMesg doughnutsFieldDescMesg = new FieldDescriptionMesg();
        doughnutsFieldDescMesg.setDeveloperDataIndex((short) 0);
        doughnutsFieldDescMesg.setFieldDefinitionNumber((short) 0);
        doughnutsFieldDescMesg.setFitBaseTypeId(FitBaseType.FLOAT32);
        doughnutsFieldDescMesg.setUnits(0, "doughnuts");
        doughnutsFieldDescMesg.setNativeMesgNum(MesgNum.SESSION);
        messages.add(doughnutsFieldDescMesg);

        FieldDescriptionMesg hrFieldDescMesg = new FieldDescriptionMesg();
        hrFieldDescMesg.setDeveloperDataIndex((short) 0);
        hrFieldDescMesg.setFieldDefinitionNumber((short) 1);
        hrFieldDescMesg.setFitBaseTypeId(FitBaseType.UINT8);
        hrFieldDescMesg.setFieldName(0, "Heart Rate");
        hrFieldDescMesg.setUnits(0, "bpm");
        hrFieldDescMesg.setNativeFieldNum((short) RecordMesg.HeartRateFieldNum);
        hrFieldDescMesg.setNativeMesgNum(MesgNum.RECORD);
        messages.add(hrFieldDescMesg);


        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            RecordMesg recordMesg = new RecordMesg();
            recordMesg.setTimestamp(point.getTime());
            recordMesg.setPositionLat((int) (degree * (Double.parseDouble(point.getLat()))));
            recordMesg.setPositionLong((int) (degree * (Double.parseDouble(point.getLon()))));
            if (point.getEle() != null) {
                recordMesg.setAltitude(Float.parseFloat(point.getEle()));
            }

//            recordMesg.setDistance((float) i);
//            recordMesg.setSpeed((float) 1);
//            recordMesg.setHeartRate((short) ((Math.sin(twoPI * (0.01 * i + 10)) + 1.0) * 127.0)); // Sine
//            recordMesg.setCadence((short) (i % 255)); // Sawtooth
//            recordMesg.setPower(((short) (i % 255) < 157 ? 150 : 250)); //Square

            // Add a Developer Field to the Record Message
//            DeveloperField hrDevField = new DeveloperField(hrFieldDescMesg, developerIdMesg);
//            recordMesg.addDeveloperField(hrDevField);
//            hrDevField.setValue((short) (Math.sin(twoPI * (.01 * i + 10)) + 1.0) * 127.0);

            // Write the Record message to the output stream
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
        lapMesg.setTotalElapsedTime((float) (endTime.getTimestamp() - startTime.getTimestamp()));
        lapMesg.setTotalTimerTime((float) (endTime.getTimestamp() - startTime.getTimestamp())); //todo
        messages.add(lapMesg);

        // Every FIT ACTIVITY file MUST contain at least one Session message
        SessionMesg sessionMesg = new SessionMesg();
        sessionMesg.setMessageIndex(0);
        sessionMesg.setTimestamp(startTime);
        sessionMesg.setStartTime(startTime);
        sessionMesg.setTotalElapsedTime((float) (endTime.getTimestamp() - startTime.getTimestamp()));
        sessionMesg.setTotalTimerTime((float) (endTime.getTimestamp() - startTime.getTimestamp())); //todo
        sessionMesg.setSport(session.getSport());
        sessionMesg.setSubSport(SubSport.GENERIC);
        sessionMesg.setFirstLapIndex(0);
        sessionMesg.setNumLaps(1);
        messages.add(sessionMesg);

        // Add a Developer Field to the Session message
        DeveloperField doughnutsEarnedDevField = new DeveloperField(doughnutsFieldDescMesg, developerIdMesg);
        doughnutsEarnedDevField.setValue(sessionMesg.getTotalElapsedTime() / 1200.0f);
        sessionMesg.addDeveloperField(doughnutsEarnedDevField);

        // Every FIT ACTIVITY file MUST contain EXACTLY one Activity message
        ActivityMesg activityMesg = new ActivityMesg();
        activityMesg.setTimestamp(endTime);
        activityMesg.setNumSessions(1);
        activityMesg.setLocalTimestamp(endTime.getTimestamp()); //todo
        activityMesg.setTotalTimerTime((float) (endTime.getTimestamp() - startTime.getTimestamp()));
        messages.add(activityMesg);

        CreateActivityFile(messages, filename, startTime);
    }
}
