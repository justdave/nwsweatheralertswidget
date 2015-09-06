/* started with sample code from  http://mobile.tutsplus.com/tutorials/android/android-sdk-build-a-simple-sax-parser/ */

package net.justdave.nwsweatheralertswidget;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NWSEventHandler extends DefaultHandler {
    StringBuilder elementValue;
    Boolean elementOn = false;
    NWSAlertEntryDetail data = new NWSAlertEntryDetail();
    public NWSAlertEntryDetail getXMLData() {
        return data;
    }

    /**
     * This will be called when the tags of the XML starts.
     **/
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementValue = new StringBuilder();
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
                if (localName.equalsIgnoreCase("identifier")) {
                    data.setIdentifier(elementValue.toString());
                } else if (localName.equalsIgnoreCase("event")) {
                    data.setEvent(elementValue.toString());
                } else if (localName.equalsIgnoreCase("description")) {
                    data.setDescription(elementValue.toString());
                } else if (localName.equalsIgnoreCase("instruction")) {
                    data.setInstruction(elementValue.toString());
                } else if (localName.equalsIgnoreCase("effective")) {
                    data.setEffective(elementValue.toString());
                } else if (localName.equalsIgnoreCase("expires")) {
                    data.setExpires(elementValue.toString());
                } else if (localName.equalsIgnoreCase("status")) {
                    data.setStatus(elementValue.toString());
                } else if (localName.equalsIgnoreCase("msgType")) {
                    data.setMsgType(elementValue.toString());
                } else if (localName.equalsIgnoreCase("category")) {
                    data.setCategory(elementValue.toString());
                } else if (localName.equalsIgnoreCase("urgency")) {
                    data.setUrgency(elementValue.toString());
                } else if (localName.equalsIgnoreCase("severity")) {
                    data.setSeverity(elementValue.toString());
                } else if (localName.equalsIgnoreCase("certainty")) {
                    data.setCertainty(elementValue.toString());
                } else if (localName.equalsIgnoreCase("areaDesc")) {
                    data.setAreaDesc(elementValue.toString());
                }

}
    /**
     * This is called to get the tags value
     **/
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (elementValue != null) {
            elementValue.append(new String(ch, start, length));
        }
    }
}
