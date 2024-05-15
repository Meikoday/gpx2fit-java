package com.yangyang5214;


import com.garmin.fit.*;
import com.yangyang5214.gpx2fit.gpx.GpxParser;
import com.yangyang5214.gpx2fit.model.Point;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


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
        List<Point> points = gpxParser.parser();
        if (points == null) {
            return;
        }
        System.out.format("Find point size %d\n", points.size());
        encodeActivity(points);
    }

    private static void encodeActivity(List<Point> points) {
        FileEncoder encode;
        try {
            encode = new FileEncoder(new java.io.File("result.fit"), Fit.ProtocolVersion.V2_0);
        } catch (FitRuntimeException e) {
            e.printStackTrace();
            return;
        }

        // 写入文件头信息
        FileIdMesg fileIdMesg = new FileIdMesg();
        fileIdMesg.setType(File.ACTIVITY);
        fileIdMesg.setManufacturer(Manufacturer.DEVELOPMENT);
        encode.write(fileIdMesg);

        // 写入记录数据
        for (Point point : points) {
            RecordMesg record = new RecordMesg();
            record.setTimestamp(convertToDateTime(point.getTime()));
            record.setPositionLat((int) (degree * (Double.parseDouble(point.getLat()))));
            record.setPositionLong((int) (degree * (Double.parseDouble(point.getLon()))));
            if (point.getEle() != null) {
                record.setAltitude(Float.parseFloat(point.getEle()));
            }
            encode.write(record);
        }

        // 关闭编码器
        try {
            encode.close();
            System.out.println("FIT file generated successfully!");
        } catch (FitRuntimeException e) {
            System.err.println("Error closing encoder.");
        }
    }

    private static DateTime convertToDateTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(time);
            return new DateTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}