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

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructors();
        testAccessors();
        testToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
