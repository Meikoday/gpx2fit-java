package com.yangyang5214.gpx2fit.gpx;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.yangyang5214.gpx2fit.model.Point;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

public class GpxParser {


    private String xmlFile;

    public GpxParser(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public List<Point> parser() {
        List<Point> points = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            Document document = db.parse(xmlFile);
            NodeList trkpts = document.getElementsByTagName("trkpt");

            int len = trkpts.getLength();
            points = new ArrayList<>(len);

            for (int i = 0; i < len; i++) {
                Node trkpt = trkpts.item(i);
                Element trkptElm = (Element) trkpt;

                NodeList times = trkptElm.getElementsByTagName("time");
                if (times == null) {
                    System.err.println("not found time attr");
                    return null;
                }

                Point point = new Point();
                point.setLon(trkptElm.getAttribute("lon"));
                point.setLat(trkptElm.getAttribute("lat"));
                point.setTime(times.item(0).getTextContent());
                NodeList eles = trkptElm.getElementsByTagName("ele");
                if (eles != null) {
                    point.setEle(eles.item(0).getTextContent());
                }

                //todo with extensions

                points.add(point);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return points;
    }
}
