/*
 * @test
 * @summary javax.xml.stream.util.XMLEventConsumer conformance: white-box structure + black-box behavior.
 * @run main XMLEventConsumerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventConsumerTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.stream.util.XMLEventConsumer");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "add"), "method add");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.util.XMLEventConsumer a = new javax.xml.stream.util.XMLEventConsumer(){
            public void add(javax.xml.stream.events.XMLEvent p0) { }
        };
        check(a != null, "anon impl of XMLEventConsumer");
        a.add((javax.xml.stream.events.XMLEvent) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (reader -> consumer collection closed loop) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();

        // 1) XMLEventWriter is an XMLEventConsumer
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        check(ew instanceof javax.xml.stream.util.XMLEventConsumer, "XMLEventWriter is an XMLEventConsumer");
        ew.close();

        // 2) feed a full reader into a collecting consumer: count + identity + content
        final java.util.List<javax.xml.stream.events.XMLEvent> collected =
                new java.util.ArrayList<javax.xml.stream.events.XMLEvent>();
        javax.xml.stream.util.XMLEventConsumer cons =
                new javax.xml.stream.util.XMLEventConsumer() {
                    public void add(javax.xml.stream.events.XMLEvent e) { collected.add(e); }
                };
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1'>t<c/></r>"));
        int fed = 0;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            cons.add(e);
            fed++;
        }
        r.close();
        check(fed == 7 && collected.size() == 7, "7 events fed and collected");
        int[] expected = {7, 1, 4, 1, 2, 2, 8};
        boolean seq = true;
        for (int i = 0; i < 7; i++) seq &= collected.get(i).getEventType() == expected[i];
        check(seq, "collected event sequence 7,1,4,1,2,2,8");
        check(collected.get(1).asStartElement().getAttributeByName(
                new javax.xml.namespace.QName("a")).getValue().equals("1"), "collected SE attribute intact");
        check(collected.get(2).asCharacters().getData().equals("t"), "collected characters intact");
        check(collected.get(6).isEndDocument(), "collected end document");

        // 3) reader -> writer (an XMLEventConsumer) -> exact serialized output
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew2 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos2);
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1'>t<c/></r>"));
        while (r2.hasNext()) ew2.add(r2.nextEvent());
        ew2.flush();
        r2.close();
        String out = new String(bos2.toByteArray(), "UTF-8");
        check(out.equals("<?xml version=\"1.0\" ?><r a=\"1\">t<c></c></r>"), "writer serialized output exact");
        ew2.close();

        // 4) XMLEventWriter.add(XMLEventReader) copies the whole stream in one call
        java.io.ByteArrayOutputStream bos3 = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew3 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos3);
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1'>t<c/></r>"));
        ew3.add(r3);
        ew3.flush();
        r3.close();
        check(new String(bos3.toByteArray(), "UTF-8").equals("<?xml version=\"1.0\" ?><r a=\"1\">t<c></c></r>"),
                "add(XMLEventReader) whole-stream copy");
        ew3.close();

        // 5) null handling: custom consumer accepts null without contract violation
        final int[] nulls = {0};
        javax.xml.stream.util.XMLEventConsumer nullCons =
                new javax.xml.stream.util.XMLEventConsumer() {
                    public void add(javax.xml.stream.events.XMLEvent e) { if (e == null) nulls[0]++; }
                };
        nullCons.add(null);
        check(nulls[0] == 1, "custom consumer saw null");
        // XMLEventWriter.add(null) -> NPE
        java.io.ByteArrayOutputStream bos4 = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew4 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos4);
        try { ew4.add((javax.xml.stream.events.XMLEvent) null); check(false, "writer.add(null) must NPE"); }
        catch (NullPointerException npe) { passed++; }
        ew4.close();

        // 6) custom consumer throwing propagates raw exception to the caller
        final javax.xml.stream.XMLStreamException sent =
                new javax.xml.stream.XMLStreamException("boom");
        javax.xml.stream.util.XMLEventConsumer throwing =
                new javax.xml.stream.util.XMLEventConsumer() {
                    public void add(javax.xml.stream.events.XMLEvent e) throws javax.xml.stream.XMLStreamException {
                        throw sent;
                    }
                };
        try {
            throwing.add(collected.get(0));
            check(false, "throwing consumer must propagate");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(xse == sent, "same exception object relayed");
        }
    }
}
