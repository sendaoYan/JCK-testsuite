/*
 * @test
 * @summary javax.management.AttributeChangeNotification conformance: full
 *          contract coverage. Constructor/type constant, attribute name/type/
 *          old/new value accessors incl. null old/new values, inheritance from
 *          Notification, and serialization round-trip.
 * @run main AttributeChangeNotificationTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public class AttributeChangeNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static AttributeChangeNotification roundTrip(AttributeChangeNotification n) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (AttributeChangeNotification) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        Object oldV = "old", newV = "new";
        AttributeChangeNotification n = new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", "Count", "int", oldV, newV);
        check("Count".equals(n.getAttributeName()), "attribute name");
        check("int".equals(n.getAttributeType()), "attribute type");
        check(oldV.equals(n.getOldValue()), "old value");
        check(newV.equals(n.getNewValue()), "new value");
        check("jmx.attribute.change".equals(n.getType()), "type");
        check(AttributeChangeNotification.ATTRIBUTE_CHANGE.equals("jmx.attribute.change"), "ATTRIBUTE_CHANGE constant");
    }

    static void testInheritanceAndDefaults() {
        AttributeChangeNotification n = new AttributeChangeNotification("src", 3L, 4L, "msg", "Count", "int", "old", "new");
        check(n instanceof Notification, "extends Notification");
        check(n.getSource().equals("src"), "source");
        check(n.getSequenceNumber() == 3L, "sequence");
        check(n.getTimeStamp() == 4L, "timestamp");
        check(n.getMessage().equals("msg"), "message");
        check("jmx.attribute.change".equals(n.getType()), "type is ATTRIBUTE_CHANGE");
    }

    static void testNullOldNew() {
        AttributeChangeNotification n = new AttributeChangeNotification("src", 1L, 2L, "msg", "Count", "int", null, null);
        check(n.getOldValue() == null && n.getNewValue() == null, "null old/new values");
    }

    static void testSerialization() throws Exception {
        AttributeChangeNotification n = new AttributeChangeNotification("src", 9L, 10L, "msg", "Count", "int", Integer.valueOf(1), Integer.valueOf(2));
        AttributeChangeNotification back = roundTrip(n);
        check("Count".equals(back.getAttributeName()), "round trip attribute name");
        check("int".equals(back.getAttributeType()), "round trip attribute type");
        check(Integer.valueOf(1).equals(back.getOldValue()), "round trip old value");
        check(Integer.valueOf(2).equals(back.getNewValue()), "round trip new value");
        check(back.getSequenceNumber() == 9L && back.getTimeStamp() == 10L, "round trip seq/timestamp");
    }

    // ---- deepened (batch d): hardcoded type / value identity / broadcaster+filter routing ----

    static void testTypeHardcoded() {
        // the constructor takes no type parameter: the type is always ATTRIBUTE_CHANGE
        Notification n = new AttributeChangeNotification("s", 1L, 2L, "m", "A", "int", 0, 1);
        check("jmx.attribute.change".equals(n.getType()), "type hardcoded to jmx.attribute.change");
        check(AttributeChangeNotification.ATTRIBUTE_CHANGE.equals("jmx.attribute.change"),
                "ATTRIBUTE_CHANGE constant value locked");
        check(Notification.class.getName().equals("javax.management.Notification")
                && n instanceof Notification, "extends Notification");
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

    public static void main(String[] args) throws Exception {
        testBasic();
        testInheritanceAndDefaults();
        testNullOldNew();
        testSerialization();
        testTypeHardcoded();
        testValueIdentity();
        testThroughBroadcasterAndFilter();
        testToStringInherits();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
