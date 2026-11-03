/*
 * @test
 * @summary javax.management.Notification conformance: white-box structure + black-box behavior.
 * @run main NotificationTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.Notification");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == java.util.EventObject.class, "extends EventObject");
        check(hasMethod(c, "getType"), "has getType");
        check(hasMethod(c, "getSequenceNumber"), "has getSequenceNumber");
        check(hasMethod(c, "getTimeStamp"), "has getTimeStamp");
        check(hasMethod(c, "getMessage"), "has getMessage");
        check(hasMethod(c, "getUserData"), "has getUserData");
        check(hasMethod(c, "setSource"), "has setSource");
        check(hasMethod(c, "setSequenceNumber"), "has setSequenceNumber");
        check(hasMethod(c, "setTimeStamp"), "has setTimeStamp");
        check(hasMethod(c, "setUserData"), "has setUserData");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object source = "src";
        long seq = 7L;
        long ts = 1000L;
        String type = "test.type";
        String msg = "hello";

        Notification n = new Notification(type, source, seq, ts, msg);
        check(n.getType().equals(type), "getType");
        check(n.getSource() == source, "getSource");
        check(n.getSequenceNumber() == seq, "getSequenceNumber");
        check(n.getTimeStamp() == ts, "getTimeStamp");
        check(n.getMessage().equals(msg), "getMessage");
        check(n.getUserData() == null, "userData default null");

        // 3-arg constructor sets timestamp from current time and no message
        Notification n3 = new Notification(type, source, seq);
        check(n3.getType().equals(type) && n3.getSequenceNumber() == seq, "3-arg ctor type/seq");
        check("".equals(n3.getMessage()), "3-arg ctor empty message");

        // setters
        n.setSequenceNumber(100L);
        n.setTimeStamp(200L);
        n.setUserData("data");
        n.setSource("newSource");
        check(n.getSequenceNumber() == 100L, "setSequenceNumber");
        check(n.getTimeStamp() == 200L, "setTimeStamp");
        check("data".equals(n.getUserData()), "setUserData");
        check("newSource".equals(n.getSource()), "setSource");

        // toString non-null and contains type
        check(n.toString() != null && n.toString().contains(type), "toString contains type");

        // sequence number defaults to 0 in type/source/seq constructor with timestamp form
        Notification n4 = new Notification(type, source, seq, ts);
        check("".equals(n4.getMessage()), "4-arg ctor empty message");
        check(n4.getSequenceNumber() == seq && n4.getTimeStamp() == ts, "4-arg ctor seq/timestamp");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
