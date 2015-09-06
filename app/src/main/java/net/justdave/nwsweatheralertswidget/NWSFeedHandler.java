/* started with sample code from  http://mobile.tutsplus.com/tutorials/android/android-sdk-build-a-simple-sax-parser/ */

package net.justdave.nwsweatheralertswidget;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NWSFeedHandler extends DefaultHandler {
    String elementValue = null;
    Boolean elementOn = false;
    Boolean feedActive = false;
    NWSAlertEntry currentEntry = null;
    NWSAlertList data = new NWSAlertList();
    public NWSAlertList getXMLData() {
        return data;
    }

    /**
     * This will be called when the tags of the XML starts.
     **/
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementOn = true;
        if (localName.equalsIgnoreCase("feed")) {
            data.clear();
            feedActive = true;
        } else if (feedActive) {
            if (localName.equals("entry")) {
                currentEntry = new NWSAlertEntry();
            } else if (currentEntry != null) {
                /* Here we get data that needs to be retrieved from attributes */
                if (localName.equals("link")) {
                    String href = attributes.getValue("href");
                    currentEntry.setLink(href);
                }
            }
        }
    }
    /**
     * This will be called when the tags of the XML end.
     **/
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elementOn = false;
        /**
         * Sets the values after retrieving the values from the XML tags
         * */
        if (feedActive) {
            if (currentEntry != null) {
                if (localName.equalsIgnoreCase("id"))
                    currentEntry.setId(elementValue);
                else if (localName.equalsIgnoreCase("entry")) {
                    if (!currentEntry.getEvent().equals("")) {
                        // if Event is blank, it means this is the fake entry we get
                        // when the feed is empty, so ignore it.
                        data.add(currentEntry);
                    }
                    currentEntry = null;
                } else if (localName.equalsIgnoreCase("updated")) {
                    currentEntry.setUpdated(elementValue);
                } else if (localName.equalsIgnoreCase("published")) {
                    currentEntry.setPublished(elementValue);
                } else if (localName.equalsIgnoreCase("title")) {
                    currentEntry.setTitle(elementValue);
                } else if (localName.equalsIgnoreCase("summary")) {
                    currentEntry.setSummary(elementValue);
                } else if (localName.equalsIgnoreCase("event")) {
                    currentEntry.setEvent(elementValue);
                } else if (localName.equalsIgnoreCase("effective")) {
                    currentEntry.setEffective(elementValue);
                } else if (localName.equalsIgnoreCase("expires")) {
                    currentEntry.setExpires(elementValue);
                } else if (localName.equalsIgnoreCase("status")) {
                    currentEntry.setStatus(elementValue);
                } else if (localName.equalsIgnoreCase("msgType")) {
                    currentEntry.setMsgType(elementValue);
                } else if (localName.equalsIgnoreCase("category")) {
                    currentEntry.setCategory(elementValue);
                } else if (localName.equalsIgnoreCase("urgency")) {
                    currentEntry.setUrgency(elementValue);
                } else if (localName.equalsIgnoreCase("severity")) {
                    currentEntry.setSeverity(elementValue);
                } else if (localName.equalsIgnoreCase("certainty")) {
                    currentEntry.setCertainty(elementValue);
                } else if (localName.equalsIgnoreCase("areaDesc")) {
                    currentEntry.setAreaDesc(elementValue);
                }
            } else if (localName.equalsIgnoreCase("feed")) {
                feedActive = false;
            }
        }
    }
    /**
     * This is called to get the tags value
     **/
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (elementOn) {
            elementValue = new String(ch, start, length);
            elementOn = false;
        }
    }
}
