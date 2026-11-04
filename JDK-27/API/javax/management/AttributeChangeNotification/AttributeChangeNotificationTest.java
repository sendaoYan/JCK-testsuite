/*
 * @test
 * @summary javax.management.AttributeChangeNotification conformance: white-box structure + black-box behavior.
 * @run main AttributeChangeNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class AttributeChangeNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.AttributeChangeNotification");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == Notification.class, "extends Notification");
        check(hasMethod(c, "getAttributeName"), "has getAttributeName");
        check(hasMethod(c, "getAttributeType"), "has getAttributeType");
        check(hasMethod(c, "getOldValue"), "has getOldValue");
        check(hasMethod(c, "getNewValue"), "has getNewValue");
        check("jmx.attribute.change".equals(AttributeChangeNotification.ATTRIBUTE_CHANGE), "ATTRIBUTE_CHANGE constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object source = "source";
        long seq = 42L;
        long ts = 123456789L;
        AttributeChangeNotification n = new AttributeChangeNotification(
                source, seq, ts, "message", "attrName", "java.lang.Integer", 1, 2);

        check(n.getSource() == source, "getSource");
        check(n.getSequenceNumber() == seq, "getSequenceNumber");
        check(n.getTimeStamp() == ts, "getTimeStamp");
        check("message".equals(n.getMessage()), "getMessage");
        check("attrName".equals(n.getAttributeName()), "getAttributeName");
        check("java.lang.Integer".equals(n.getAttributeType()), "getAttributeType");
        check(n.getOldValue().equals(1), "getOldValue");
        check(n.getNewValue().equals(2), "getNewValue");
        check(n.getType().equals(AttributeChangeNotification.ATTRIBUTE_CHANGE), "type is ATTRIBUTE_CHANGE");
        check(AttributeChangeNotification.ATTRIBUTE_CHANGE.equals("jmx.attribute.change"), "constant value");

        // null old/new values are preserved
        AttributeChangeNotification n2 = new AttributeChangeNotification(
                source, 1L, 2L, "m", "a", "t", null, null);
        check(n2.getOldValue() == null, "null old value");
        check(n2.getNewValue() == null, "null new value");
        check(n2.getAttributeName().equals("a") && n2.getAttributeType().equals("t"), "null-value notification fields");
    }

    // ---- deepened (batch d): hardcoded type / value identity / routing / serial form ----

    static AttributeChangeNotification roundTrip(AttributeChangeNotification n) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(n);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        AttributeChangeNotification back = (AttributeChangeNotification) ois.readObject();
        ois.close();
        return back;
    }

    static void testTypeHardcoded() {
        // the constructor takes no type parameter: the type is always ATTRIBUTE_CHANGE
        Notification n = new AttributeChangeNotification("s", 1L, 2L, "m", "A", "int", 0, 1);
        check("jmx.attribute.change".equals(n.getType()), "type hardcoded to jmx.attribute.change");
        check(AttributeChangeNotification.ATTRIBUTE_CHANGE.equals("jmx.attribute.change"),
                "ATTRIBUTE_CHANGE constant value locked");
        // null attribute name / type are preserved verbatim (no validation in ctor)
        AttributeChangeNotification nullName = new AttributeChangeNotification(
                "s", 1L, 2L, "m", null, null, null, null);
        check(nullName.getAttributeName() == null && nullName.getAttributeType() == null,
                "null attribute name/type preserved");
    }

    static void testValueIdentity() {
        Object oldV = new Object();
        Object newV = new Object();
        AttributeChangeNotification n = new AttributeChangeNotification(
                "s", 1L, 2L, "m", "A", "java.lang.Object", oldV, newV);
        check(n.getOldValue() == oldV && n.getNewValue() == newV,
                "old/new values are the very same instances");
        check(n.getUserData() == null, "userData not auto-populated by the ACN constructor");
        n.setUserData(newV);
        check(n.getUserData() == newV, "setUserData independent of the new-value field");
        // attribute type string is opaque: any string round-trips
        check("weird/type.<x>".equals(new AttributeChangeNotification(
                "s", 1L, 2L, "m", "A", "weird/type.<x>", null, null).getAttributeType()),
                "attribute type string round-trips verbatim");
    }

    static void testThroughBroadcasterAndFilter() throws Exception {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport(
                new MBeanNotificationInfo(new String[]{"jmx.attribute.change"},
                        AttributeChangeNotification.class.getName(), "d"));
        AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
        filter.enableAttribute("Count");
        final List<Notification> seen = new ArrayList<Notification>();
        Object handback = new Object();
        final List<Object> hbs = new ArrayList<Object>();
        nbs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); hbs.add(h); }
        }, filter, handback);

        AttributeChangeNotification dropped = new AttributeChangeNotification(
                nbs, 6L, 7L, "changed", "Other", "int", 1, 2);
        nbs.sendNotification(dropped);
        // synchronous delivery with the default executor: a short bounded quiet window
        for (long deadline = System.currentTimeMillis() + 300L;
             System.currentTimeMillis() < deadline; ) Thread.sleep(20L);
        check(seen.isEmpty(), "ACN for a non-enabled attribute is filtered out");

        AttributeChangeNotification routed = new AttributeChangeNotification(
                nbs, 5L, 6L, "changed", "Count", "int", 1, 2);
        nbs.sendNotification(routed);
        check(seen.size() == 1 && seen.get(0) == routed,
                "ACN for the enabled attribute routed through broadcaster+filter");
        check(hbs.size() == 1 && hbs.get(0) == handback, "handback delivered with the routed ACN");
        AttributeChangeNotification got = (AttributeChangeNotification) seen.get(0);
        check("Count".equals(got.getAttributeName()) && "int".equals(got.getAttributeType())
                && Integer.valueOf(1).equals(got.getOldValue())
                && Integer.valueOf(2).equals(got.getNewValue()),
                "routed ACN keeps its attribute payload");
    }

    static void testToStringInherits() {
        AttributeChangeNotification n = new AttributeChangeNotification("s", 1L, 2L, "m", "A", "int", 0, 1);
        String s = n.toString();
        check(s.contains("[type=jmx.attribute.change]"), "toString carries the hardcoded type");
        check(s.contains("[message=m]"), "toString carries the message");
        check(s.startsWith("javax.management.AttributeChangeNotification["),
                "toString prefix is the ACN class name");
    }

    static void testSerializationDeep() throws Exception {
        AttributeChangeNotification n = new AttributeChangeNotification(
                "src", 9L, 10L, "msg", "Count", "java.lang.Integer",
                Integer.valueOf(1), Integer.valueOf(2));
        AttributeChangeNotification back = roundTrip(n);
        check("Count".equals(back.getAttributeName()), "round trip keeps attribute name");
        check("java.lang.Integer".equals(back.getAttributeType()), "round trip keeps attribute type");
        check(Integer.valueOf(1).equals(back.getOldValue()), "round trip keeps old value");
        check(Integer.valueOf(2).equals(back.getNewValue()), "round trip keeps new value");
        check(back.getSequenceNumber() == 9L && back.getTimeStamp() == 10L,
                "round trip keeps sequence and timestamp");
        check("jmx.attribute.change".equals(back.getType()), "round trip keeps hardcoded type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTypeHardcoded();
        testValueIdentity();
        testThroughBroadcasterAndFilter();
        testToStringInherits();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
