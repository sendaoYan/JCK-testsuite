/*
 * @test
 * @summary javax.naming.StringRefAddr conformance: full contract coverage.
 *          type/content/toString, equals/hashCode (type + content), null
 *          content handling, and serialization round-trip.
 * @run main StringRefAddrTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.naming.StringRefAddr;

public class StringRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static StringRefAddr roundTrip(StringRefAddr a) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(a); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (StringRefAddr) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        StringRefAddr a = new StringRefAddr("url", "http://x/");
        check("url".equals(a.getType()), "getType");
        check("http://x/".equals(a.getContent()), "getContent");
        check(a.toString() != null, "toString");
    }

    static void testEqualsHashCode() {
        StringRefAddr a = new StringRefAddr("t", "v");
        StringRefAddr b = new StringRefAddr("t", "v");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(new StringRefAddr("t", "w")), "different content");
        check(!a.equals(new StringRefAddr("u", "v")), "different type");
        check(!a.equals(null) && !a.equals("t"), "equals null/non-RefAddr");
    }

    static void testNullContent() {
        StringRefAddr a = new StringRefAddr("t", null);
        check(a.getContent() == null, "null content getContent");
        check(a.toString() != null, "null content toString");
        StringRefAddr b = new StringRefAddr("t", null);
        check(a.equals(b), "null content equals");
    }

    static void testSerialization() throws Exception {
        StringRefAddr a = new StringRefAddr("url", "http://x/");
        StringRefAddr back = roundTrip(a);
        check(back.equals(a) && "http://x/".equals(back.getContent()), "round trip");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEqualsHashCode();
        testNullContent();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
