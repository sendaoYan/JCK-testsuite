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

    // ---- deepened (batch d): ctor matrix / null semantics / identity equals / serial form ----

    static Notification roundTrip(Notification n) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(n);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Notification back = (Notification) ois.readObject();
        ois.close();
        return back;
    }

    static void testCtorEdges() {
        long before = System.currentTimeMillis();
        Notification a = new Notification("t.a", "s", -5L);
        long after = System.currentTimeMillis();
        check(a.getSequenceNumber() == -5L, "negative sequence number is legal");
        check(a.getTimeStamp() >= before && a.getTimeStamp() <= after,
                "3-arg ctor fills timeStamp from the clock");
        check(a.getMessage() != null && a.getMessage().length() == 0,
                "3-arg ctor message defaults to empty string");

        Notification b = new Notification("t.b", "s", Long.MIN_VALUE);
        check(b.getSequenceNumber() == Long.MIN_VALUE, "Long.MIN_VALUE sequence legal");
        check(b.getUserData() == null, "userData defaults to null");

        Notification c = new Notification("t.c", "s", 0L, 0L);
        check(c.getTimeStamp() == 0L, "explicit timeStamp=0 preserved (no clock fill)");
        Notification d = new Notification("t.d", "s", 1L, -1L);
        check(d.getTimeStamp() == -1L, "negative timeStamp preserved");

        Notification e = new Notification("t.e", "s", 1L, 5L, null);
        check(e.getMessage() == null, "explicit null message preserved by 5-arg ctor");
        Notification f = new Notification("t.f", "s", 1L, null);
        check(f.getMessage() == null, "explicit null message preserved by 4-arg ctor");
        check(f.getTimeStamp() > 0, "(type,source,seq,message) ctor still fills timeStamp");

        Object src = new Object();
        Notification g = new Notification("t.g", src, 1L);
        check(g.getSource() == src, "getSource returns the identical source instance");
        check(((java.util.EventObject) g).getSource() == src,
                "EventObject.getSource and Notification.getSource agree");
    }

    static void testNullSemantics() {
        // null source is rejected by the inherited java.util.EventObject constructor
        try {
            new Notification("t", null, 1L);
            check(false, "null source must be rejected");
        } catch (IllegalArgumentException ex) {
            check(true, "null source -> IllegalArgumentException (from EventObject ctor)");
        }
        // null type is NOT rejected by the implementation (no check in any ctor),
        // although the javadoc mentions IllegalArgumentException; actual behavior locked
        Notification n = new Notification(null, "s", 1L);
        check(n.getType() == null, "null type accepted, getType() returns null");
        check(n.toString().contains("type=null"), "toString renders null type");
        // after construction the source may be replaced with null
        n.setSource(null);
        check(n.getSource() == null, "setSource(null) allowed after construction");
    }

    static void testEqualsIsIdentity() {
        Notification a = new Notification("t", "s", 1L, 2L, "m");
        Notification b = new Notification("t", "s", 1L, 2L, "m");
        check(!a.equals(b), "no equals override: equal-field notifications are not equals");
        check(a.equals(a), "equals is identity: equals(self) true");
        check(a.hashCode() == a.hashCode(), "hashCode stable between calls");
        boolean hasCompareTo = false;
        for (Method m : Notification.class.getMethods())
            if (m.getName().equals("compareTo")) hasCompareTo = true;
        check(!hasCompareTo, "Notification declares no compareTo");
    }

    static void testToStringExact() {
        Notification n = new Notification("my.type", "src-obj", 7L, 42L, "hello");
        check("javax.management.Notification[source=src-obj][type=my.type][message=hello]"
                .equals(n.toString()),
                "toString exact format: EventObject toString + [type=][message=]");
        Notification m = new Notification("t", "s", 1L);
        check(m.toString().endsWith("[message=]"), "toString with default empty message");
    }

    static void testSerializationDeep() throws Exception {
        boolean sealed = false;
        long svuidValue = 0L;
        try {
            java.lang.reflect.Field svuid = Notification.class.getDeclaredField("serialVersionUID");
            svuid.setAccessible(true);
            svuidValue = svuid.getLong(null);
        } catch (Throwable ex) {
            // JDK 9+ strong encapsulation: java.management does not open javax.management.
            // The exception class does not exist on JDK 8, so compare by name.
            if ("java.lang.reflect.InaccessibleObjectException".equals(ex.getClass().getName())) {
                sealed = true;
            } else {
                throw ex;
            }
        }
        check(sealed || svuidValue == -7516092053498031989L,
                "serialVersionUID is -7516092053498031989L (or sealed by strong encapsulation)");
        Notification n = new Notification("deep.type", "src", -9L, 12345L, "msg");
        n.setUserData(java.util.Arrays.asList("x", "y"));
        Notification back = roundTrip(n);
        check(back.getSequenceNumber() == -9L, "round trip keeps negative sequence");
        check("deep.type".equals(back.getType()) && "msg".equals(back.getMessage()),
                "round trip keeps type and message");
        check(java.util.Arrays.asList("x", "y").equals(back.getUserData()),
                "round trip keeps userData value");
        Notification n2 = new Notification("t", "s1", 1L);
        n2.setSource("s2");
        check("s2".equals(roundTrip(n2).getSource()), "round trip keeps replaced source");
        check(roundTrip(n2).getTimeStamp() > 0, "round trip keeps clock-filled timeStamp");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorEdges();
        testNullSemantics();
        testEqualsIsIdentity();
        testToStringExact();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
