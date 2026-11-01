/*
 * @test
 * @summary java.util.jar.Attributes conformance: put/getValue/remove/equals.
 * @run main AttributesTest
 */

import java.util.jar.Attributes;

public class AttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAttributes() {
        Attributes a = new Attributes();
        check(a.isEmpty() && a.size() == 0, "empty initially");
        a.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        check("1.0".equals(a.getValue(Attributes.Name.MANIFEST_VERSION)), "put/getValue(Name)");
        check("1.0".equals(a.getValue("Manifest-Version")), "getValue(String)");
        check(a.size() == 1, "size 1");
        check(a.containsKey(Attributes.Name.MANIFEST_VERSION), "containsKey(Name)");
        check(a.containsValue("1.0"), "containsValue");
        check(a.keySet().contains(Attributes.Name.MANIFEST_VERSION), "keySet");
        check(a.values().contains("1.0"), "values");

        Attributes b = new Attributes(a);
        check(b.size() == 1 && "1.0".equals(b.getValue("Manifest-Version")), "copy constructor");

        check("1.0".equals(a.remove(Attributes.Name.MANIFEST_VERSION)), "remove(Name)");
        check(a.isEmpty(), "removed");

        Attributes c = new Attributes();
        c.putValue("X", "y");
        Attributes d = new Attributes();
        d.putValue("X", "y");
        check(c.equals(d) && c.hashCode() == d.hashCode(), "equals/hashCode");
        c.clear();
        check(c.isEmpty(), "clear");
        check(a.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
