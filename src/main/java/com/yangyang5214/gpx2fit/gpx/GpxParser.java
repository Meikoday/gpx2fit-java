package com.yangyang5214.gpx2fit.gpx;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.garmin.fit.DateTime;
import com.garmin.fit.Sport;
import com.yangyang5214.gpx2fit.model.Point;
import com.yangyang5214.gpx2fit.model.Session;
import org.w3c.dom.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GpxParser {

    private String xmlFile;

    public GpxParser(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public Session parser() {
        List<Point> points = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document document = null;
        try {
            db = dbf.newDocumentBuilder();
            document = db.parse(xmlFile);
            NodeList trkpts = document.getElementsByTagName("trkpt");

            int len = trkpts.getLength();
            points = new ArrayList<>(len);

            float distance = 0;

            for (int i = 0; i < len; i++) {
                Node trkpt = trkpts.item(i);
                Element trkptElm = (Element) trkpt;

                NodeList times = trkptElm.getElementsByTagName("time");
                if (times == null) {
                    System.err.println("not found time attr");
                    return null;
                }

                Point point = new Point();
                point.setLon(Double.parseDouble(trkptElm.getAttribute("lon")));
                point.setLat(Double.parseDouble(trkptElm.getAttribute("lat")));
                point.setTime(convertToDateTime(times.item(0).getTextContent()));
                NodeList eles = trkptElm.getElementsByTagName("ele");
                if (eles != null) {
                    point.setEle(Float.parseFloat(eles.item(0).getTextContent()));
                }

                if (i != 0) {
                    distance = distance + point.calculateDistance(points.get(i - 1));
                }
                point.setDistance(distance);

                //todo with extensions
                points.add(point);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        DateTime startTime = points.get(0).getTime();
        DateTime endTime = points.get(points.size() - 1).getTime();

        Session session = new Session();
        session.setPoints(points);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setSport(getSport(document));
        session.setTotalTimerTime(endTime.getTimestamp() - startTime.getTimestamp());
        session.setTotalDistance(points.get(points.size() - 1).getDistance());

        return session;
    }

    private DateTime convertToDateTime(String time) {
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

    public Sport getSport(Document document) {
        NodeList nodeList = document.getElementsByTagName("trk");
        Element elm = (Element) nodeList.item(0);
        NodeList types = elm.getElementsByTagName("type");
        if (types == null) {
            return Sport.CYCLING; //默认自行车
        }
        Element typeElm = (Element) types.item(0);
        String type = typeElm.getTextContent();
        return Sport.valueOf(type.toUpperCase());
    }
}
