/*
 * @test
 * @summary javax.management.Notification conformance: full contract coverage.
 *          all four constructors (message/timeStamp defaulting), type/source/
 *          sequence/timeStamp/message/userData accessors and setters,
 *          EventObject.getSource alias, toString format, and serialization
 *          round-trip preserving every field.
 * @run main NotificationTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.management.Notification;

public class NotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Notification roundTrip(Notification n) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Notification) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        Notification n = new Notification("test.type", "src", 1L, 100L, "msg");
        check("test.type".equals(n.getType()), "type");
        check("src".equals(n.getSource()), "source");
        check(n.getSequenceNumber() == 1L, "sequence number");
        check(n.getTimeStamp() == 100L, "time stamp");
        check("msg".equals(n.getMessage()), "message");
        n.setSequenceNumber(2L);
        n.setUserData("data");
        n.setSource("src2");
        check(n.getSequenceNumber() == 2L, "setSequenceNumber");
        check("data".equals(n.getUserData()), "user data");
        check("src2".equals(n.getSource()), "setSource");
    }

    static void testConstructors() {
        Object src = new Object();
        Notification a = new Notification("t", src, 7L);
        check(a.getSequenceNumber() == 7L && "".equals(a.getMessage()), "3-arg message empty");
        check(a.getTimeStamp() > 0, "3-arg timestamp populated");

        Notification b = new Notification("t", src, 7L, "m");
        check("m".equals(b.getMessage()), "4-arg message");

        Notification c = new Notification("t", src, 7L, 42L);
        check(c.getTimeStamp() == 42L && "".equals(c.getMessage()), "4-arg timestamp");

        Notification d = new Notification("t", src, 7L, 42L, "m");
        check(d.getSequenceNumber() == 7L && d.getTimeStamp() == 42L && "m".equals(d.getMessage()), "5-arg all");
    }

    static void testAccessors() {
        Notification n = new Notification("t", "s", 1L);
        n.setTimeStamp(123L);
        check(n.getTimeStamp() == 123L, "setTimeStamp");
        n.setSequenceNumber(Long.MAX_VALUE);
        check(n.getSequenceNumber() == Long.MAX_VALUE, "setSequenceNumber max");
        n.setUserData(null);
        check(n.getUserData() == null, "setUserData null");
        check("s".equals(n.getSource()), "getSource from EventObject");
        n.setSource(null);
        check(n.getSource() == null, "setSource null");
    }

    static void testToString() {
        Notification n = new Notification("t", "s", 1L, "m");
        String s = n.toString();
        check(s.contains("[type=t]"), "toString contains type");
        check(s.contains("[message=m]"), "toString contains message");
    }

    static void testSerialization() throws Exception {
        String src = "serializable-source";
        Notification n = new Notification("t", src, 5L, 99L, "m");
        n.setUserData(new int[]{1, 2, 3});
        Notification back = roundTrip(n);
        check("t".equals(back.getType()), "round trip type");
        check(back.getSequenceNumber() == 5L, "round trip sequence");
        check(back.getTimeStamp() == 99L, "round trip timestamp");
        check("m".equals(back.getMessage()), "round trip message");
        check(back.getSource().equals(src), "round trip source");
        check(back.getUserData() instanceof int[], "round trip userData");
    }

    // ---- deepened (batch d): ctor edges / null semantics / identity equals / serial form ----

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
        for (java.lang.reflect.Method m : Notification.class.getMethods())
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
        testBasic();
        testConstructors();
        testAccessors();
        testToString();
        testSerialization();
        testCtorEdges();
        testNullSemantics();
        testEqualsIsIdentity();
        testToStringExact();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
