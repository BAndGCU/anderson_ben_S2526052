package com.example.anderson_ben_s2526052;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafficXmlParser {

    public static List<Situation> parse(String xml, Situation.Kind kind)
            throws XmlPullParserException, IOException {

        List<Situation> result = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xml));

        Situation cur = null;
        boolean inTo = false, inComment = false;

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {
                String tag = xpp.getName();

                if (tag.equals("situationRecord")) {
                    cur = new Situation(kind);
                    cur.id = xpp.getAttributeValue(null, "id");
                    cur.recordType = xpp.getAttributeValue(
                            "http://www.w3.org/2001/XMLSchema-instance", "type");
                }
                else if (cur != null) {
                    if      (tag.equals("generalPublicComment")) inComment = true;
                    else if (tag.equals("to"))                   inTo = true;
                    else if (tag.equals("value") && inComment && cur.comment == null)
                        cur.comment = xpp.nextText().trim();
                    else if (tag.equals("sourceIdentification")) cur.source = xpp.nextText().trim();
                    else if (tag.equals("probabilityOfOccurrence")) cur.probability = xpp.nextText().trim();
                    else if (tag.equals("validityStatus"))       cur.validityStatus = xpp.nextText().trim();
                    else if (tag.equals("overallStartTime"))     cur.startTime = xpp.nextText().trim();
                    else if (tag.equals("overallEndTime"))       cur.endTime = xpp.nextText().trim();
                    else if (tag.equals("complianceOption"))     cur.compliance = xpp.nextText().trim();
                    else if (tag.equals("delaysType"))           cur.delaysType = xpp.nextText().trim();
                    else if (tag.equals("roadOrCarriagewayOrLaneManagementType")
                            || tag.equals("abnormalTrafficType")
                            || tag.equals("roadMaintenanceType"))  cur.subType = xpp.nextText().trim();
                    else if (tag.equals("numberOfOperationalLanes"))
                        cur.lanesOperational = toInt(xpp.nextText());
                    else if (tag.equals("numberOfLanesRestricted"))
                        cur.lanesRestricted = toInt(xpp.nextText());
                    else if (tag.equals("latitude")) {
                        Double v = toDouble(xpp.nextText());
                        if (inTo) cur.toLat = v; else cur.fromLat = v;
                    }
                    else if (tag.equals("longitude")) {
                        Double v = toDouble(xpp.nextText());
                        if (inTo) cur.toLon = v; else cur.fromLon = v;
                    }
                }
            }
            else if (eventType == XmlPullParser.END_TAG) {
                String tag = xpp.getName();
                if      (tag.equals("generalPublicComment")) inComment = false;
                else if (tag.equals("to"))                   inTo = false;
                else if (tag.equals("situationRecord") && cur != null) {
                    extractRoads(cur);
                    result.add(cur);
                    cur = null;
                }
            }
            eventType = xpp.next();
        }
        return result;
    }

    private static Integer toInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static Double toDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    // Further parsing of text: pull road names out of the comment
    private static final Pattern ROAD = Pattern.compile("\\b([AM]\\d{1,4}(?:\\(M\\))?)\\b");
    private static void extractRoads(Situation s) {
        if (s.comment == null) return;
        Matcher m = ROAD.matcher(s.comment);
        while (m.find()) if (!s.roads.contains(m.group(1))) s.roads.add(m.group(1));
    }
}
