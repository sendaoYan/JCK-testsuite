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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
