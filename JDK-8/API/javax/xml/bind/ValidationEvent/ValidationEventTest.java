/*
 * @test
 * @summary javax.xml.bind.ValidationEvent conformance: severity constants, method signatures and real event channel.
 * @run main ValidationEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;


public class ValidationEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlElement String name;
        @javax.xml.bind.annotation.XmlElement int age;
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        Class<?> c = Class.forName("javax.xml.bind.ValidationEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSeverity"), "has method getSeverity");
        check(names.contains("getMessage"), "has method getMessage");
        check(names.contains("getLinkedException"), "has method getLinkedException");
        check(names.contains("getLocator"), "has method getLocator");
        testConstants(c);
        testSignatures(c);
        testRealEvents();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // severity constants: exact values locked by spec (JSR 222)
    static void testConstants(Class<?> c) throws Exception {
        check(c.getField("WARNING").getInt(null) == 0, "WARNING == 0");
        check(c.getField("ERROR").getInt(null) == 1, "ERROR == 1");
        check(c.getField("FATAL_ERROR").getInt(null) == 2, "FATAL_ERROR == 2");
        check(c.getField("WARNING").get(null).equals(Integer.valueOf(0)), "WARNING boxed value");
        check(c.getField("ERROR").get(null).equals(Integer.valueOf(1)), "ERROR boxed value");
        check(c.getField("FATAL_ERROR").get(null).equals(Integer.valueOf(2)), "FATAL_ERROR boxed value");
        check(java.lang.reflect.Modifier.isFinal(c.getField("WARNING").getModifiers()), "WARNING is final");
        check(java.lang.reflect.Modifier.isStatic(c.getField("WARNING").getModifiers()), "WARNING is static");
        check(c.getField("WARNING").getType() == int.class, "WARNING is int");
        check(c.getField("ERROR").getType() == int.class, "ERROR is int");
        check(c.getField("FATAL_ERROR").getType() == int.class, "FATAL_ERROR is int");
        // javax.xml.bind.ValidationEvent severity constants agree with helper impl domain
        check(javax.xml.bind.ValidationEvent.WARNING < javax.xml.bind.ValidationEvent.ERROR, "WARNING < ERROR");
        check(javax.xml.bind.ValidationEvent.ERROR < javax.xml.bind.ValidationEvent.FATAL_ERROR, "ERROR < FATAL_ERROR");
    }

    // method signatures: exactly 4 declared methods, all public abstract
    static void testSignatures(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 4, "exactly 4 declared methods");
        java.util.Map<String, Class<?>> rets = new java.util.HashMap<String, Class<?>>();
        for (Method m : dm) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            rets.put(m.getName(), m.getReturnType());
        }
        check(rets.get("getSeverity") == int.class, "getSeverity() returns int");
        check(rets.get("getMessage") == String.class, "getMessage() returns String");
        check(rets.get("getLinkedException") == java.lang.Throwable.class, "getLinkedException() returns Throwable");
        check(rets.get("getLocator") == Class.forName("javax.xml.bind.ValidationEventLocator"), "getLocator() returns ValidationEventLocator");
        check(c.getInterfaces().length == 0, "extends no other interface");
    }

    // real channel: RI delivers ValidationEvent instances through a handler on the Unmarshaller
    static void testRealEvents() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        java.util.List<javax.xml.bind.ValidationEvent> events =
                new java.util.ArrayList<javax.xml.bind.ValidationEvent>();
        javax.xml.bind.ValidationEventHandler recorder = new javax.xml.bind.ValidationEventHandler() {
            public boolean handleEvent(javax.xml.bind.ValidationEvent e) {
                events.add(e);
                return true;
            }
        };
        u.setEventHandler(recorder);

        // recoverable error (unexpected element): severity ERROR, message, locator populated
        Object r = u.unmarshal(new java.io.StringReader(
                "<person><name>alice</name><age>30</age><extra>x</extra></person>"));
        check(r instanceof Person, "handler returning true lets unmarshal finish");
        check(events.size() == 1, "exactly one ERROR event delivered (locked)");
        javax.xml.bind.ValidationEvent ev = events.get(0);
        check(ev.getSeverity() == javax.xml.bind.ValidationEvent.ERROR, "unexpected element event severity == ERROR");
        check(ev.getMessage() != null && ev.getMessage().contains("unexpected element"), "event message mentions unexpected element");
        check(ev.getLinkedException() == null, "unexpected element event has no linked exception (locked)");
        javax.xml.bind.ValidationEventLocator loc = ev.getLocator();
        check(loc != null, "event carries a locator");
        check(loc.getLineNumber() == 1, "locator line == 1 (single-line xml)");
        check(loc.getColumnNumber() >= 1, "locator column >= 1");
        check(loc.getOffset() == -1, "locator offset == -1 (SAX source has no offset)");
        check(loc.getURL() == null, "locator url null for Reader input");

        // fatal error (malformed XML): severity FATAL_ERROR even when handler returns true
        events.clear();
        boolean threw = false;
        try {
            u.unmarshal(new java.io.StringReader("<broken"));
        } catch (javax.xml.bind.UnmarshalException ue) {
            threw = true;
        }
        check(threw, "malformed XML still aborts with UnmarshalException");
        check(events.size() == 1, "one event delivered for malformed XML");
        check(events.get(0).getSeverity() == javax.xml.bind.ValidationEvent.FATAL_ERROR,
                "malformed XML event severity == FATAL_ERROR");
        check(events.get(0).getMessage() != null, "fatal event message non-null");
        check(events.get(0).getLinkedException() instanceof org.xml.sax.SAXParseException,
                "fatal event linked exception is SAXParseException");
    }
}
