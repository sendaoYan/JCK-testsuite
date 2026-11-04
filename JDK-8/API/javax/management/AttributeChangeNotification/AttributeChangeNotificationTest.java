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
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

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

    public static void main(String[] args) throws Exception {
        testBasic();
        testInheritanceAndDefaults();
        testNullOldNew();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
