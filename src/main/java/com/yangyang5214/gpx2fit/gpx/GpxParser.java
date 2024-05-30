package com.yangyang5214.gpx2fit.gpx;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.garmin.fit.DateTime;
import com.garmin.fit.Sport;
import com.yangyang5214.gpx2fit.model.Point;
import com.yangyang5214.gpx2fit.model.Session;
import org.w3c.dom.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GpxParser {

    private final String xmlFile;
    private final List<SimpleDateFormat> sdfs = new ArrayList<>();

    {
        sdfs.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        sdfs.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }


    public GpxParser(String xmlFile) {
        this.xmlFile = xmlFile;
    }


    public String getPointExtNs(Document document) {
        NodeList nodes = document.getElementsByTagName("gpx");
        if (nodes.getLength() == 0) {
            return null;
        }
        NamedNodeMap attributes = nodes.item(0).getAttributes();
        if (attributes.getLength() == 0) {
            return null;
        }
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            String textContent = node.getTextContent();
            if (textContent.contains("TrackPointExtension")) {
                return node.getNodeName().split(":")[1];
            }
        }
        return null;
    }

    public Session parser() {
        List<Point> points;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document document;
        String pointExtNs;

        float totalMovingTime = 0;
        float distance = 0;

        try {
            db = dbf.newDocumentBuilder();
            document = db.parse(xmlFile);

            pointExtNs = getPointExtNs(document);

            NodeList trkpts = document.getElementsByTagName("trkpt");

            int len = trkpts.getLength();
            points = new ArrayList<>(len);


            for (int i = 0; i < len; i++) {
                Node trkpt = trkpts.item(i);
                Element trkptElm = (Element) trkpt;

                NodeList times = trkptElm.getElementsByTagName("time");

                Point point = new Point();
                point.setLon(Double.parseDouble(trkptElm.getAttribute("lon")));
                point.setLat(Double.parseDouble(trkptElm.getAttribute("lat")));
                point.setTime(convertToDateTime(times.item(0).getTextContent()));

                point.setEle(parserEle(trkptElm));
                point.setSpeed(parserSpeed(trkptElm));

                NodeList extensions = trkptElm.getElementsByTagName("extensions");
                if (extensions.getLength() > 0) {
                    Element extNode = (Element) extensions.item(0);

                    String hr = getTagByName(extNode, pointExtNs, "hr");
                    if (hr != null) {
                        point.setHr(Short.parseShort(hr));
                    }

                    String cad = getTagByName(extNode, pointExtNs, "cad");
                    if (cad != null) {
                        point.setCadence(Short.parseShort(cad));
                    }
                }

                if (i != 0) {
                    Point prePoint = points.get(i - 1);
                    float subDistance = point.calculateDistance(prePoint);
                    distance = distance + subDistance;
                    if (subDistance > 0.5) {
                        long subTs = point.subTs(prePoint);
                        if (subTs < 10) { //比较宽泛
                            totalMovingTime = totalMovingTime + subTs;
                        }
                    }
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
        session.setTotalMovingTime(totalMovingTime);
        session.setTotalElapsedTime(endTime.getTimestamp() - startTime.getTimestamp());
        session.setTotalDistance(distance);
        return session;
    }

    public String getTagByName(Element element, String pointExtNs, String tag) {
        if (pointExtNs == null) {
            return null;
        }
        NodeList node = element.getElementsByTagName(pointExtNs + ":" + tag);
        if (node.getLength() == 0) {
            return null;
        }
        Element ele = (Element) node.item(0);
        return ele.getTextContent();
    }

    public DateTime convertToDateTime(String time) {
        for (SimpleDateFormat simpleDateFormat : sdfs) {
            try {
                Date date = simpleDateFormat.parse(time);
                return new DateTime(date);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public Float parserEle(Element trkptElm) {
        NodeList eles = trkptElm.getElementsByTagName("ele");
        Node elvNode = eles.item(0);
        if (elvNode != null) {
            return Float.parseFloat(elvNode.getTextContent());
        } else {
            return (float) 0;
        }
    }

    public Float parserSpeed(Element trkptElm) {
        NodeList eles = trkptElm.getElementsByTagName("speed");
        Node elvNode = eles.item(0);
        if (elvNode != null) {
            return Float.parseFloat(elvNode.getTextContent());
        } else {
            return (float) 0;
        }
    }

    public Sport getSport(Document document) {
        Sport sport = Sport.CYCLING; //default CYCLING

        NodeList nodeList = document.getElementsByTagName("trk");
        Element elm = (Element) nodeList.item(0);
        NodeList types = elm.getElementsByTagName("type");
        Element typeElm = (Element) types.item(0);
        if (typeElm == null) {
            return sport;
        }
        String type = typeElm.getTextContent();
        try {
            sport = Sport.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            //可能是中文这里就是枚举了
            if (type.indexOf("骑行") > 0) {
                return Sport.CYCLING;
            }
            if (type.indexOf("跑步") > 0) {
                return Sport.RUNNING;
            }
            System.err.format("UnKnow type %s\n", type);
        }
        return sport;
    }
}
