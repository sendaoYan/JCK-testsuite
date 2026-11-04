/*
 * @test
 * @summary javax.management.Attribute conformance: full contract coverage.
 *          constructor/getName/getValue, equals/hashCode incl. null-value
 *          semantics and cross-type asymmetry, toString format, and
 *          serialization round-trip.
 * @run main AttributeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.management.Attribute;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Attribute roundTrip(Attribute a) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(a); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Attribute) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        Attribute a = new Attribute("Count", Integer.valueOf(1));
        check("Count".equals(a.getName()), "getName");
        check(Integer.valueOf(1).equals(a.getValue()), "getValue");
    }

    static void testEqualsHashCode() {
        Attribute a = new Attribute("Count", Integer.valueOf(1));
        Attribute b = new Attribute("Count", Integer.valueOf(1));
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(new Attribute("Count", Integer.valueOf(2))), "different value");
        check(!a.equals(new Attribute("Size", Integer.valueOf(1))), "different name");
        check(!a.equals(null) && !a.equals("Count"), "equals non-Attribute/null");

        // null-value semantics
        Attribute n1 = new Attribute("Count", null);
        Attribute n2 = new Attribute("Count", null);
        check(n1.equals(n2) && n2.equals(n1), "null value equals");
        check(n1.hashCode() == n2.hashCode(), "null value hashCode");
        check(!n1.equals(a) && !a.equals(n1), "null value vs non-null asymmetric");
    }

    static void testToString() {
        check("Count = 1".equals(new Attribute("Count", Integer.valueOf(1)).toString()), "toString");
        check("Count = null".equals(new Attribute("Count", null).toString()), "toString null value");
    }

    static void testSerialization() throws Exception {
        Attribute a = new Attribute("Count", Integer.valueOf(7));
        Attribute back = roundTrip(a);
        check(a.equals(back), "round trip equal");
        check("Count".equals(back.getName()) && Integer.valueOf(7).equals(back.getValue()), "round trip fields");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEqualsHashCode();
        testToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
