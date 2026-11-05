/*
 * @test
 * @summary javax.xml.stream.Location conformance: interface hierarchy and methods.
 * @run main LocationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class LocationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.Location");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLineNumber"), "has method getLineNumber");
        check(names.contains("getColumnNumber"), "has method getColumnNumber");
        check(names.contains("getCharacterOffset"), "has method getCharacterOffset");
        check(names.contains("getPublicId"), "has method getPublicId");
        check(names.contains("getSystemId"), "has method getSystemId");
        testCustomImplContract();
        testRealParserLocations();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: implementer contract through a fixed implementation ----
    static void testCustomImplContract() {
        javax.xml.stream.Location l = new javax.xml.stream.Location() {
            public int getLineNumber() { return 12; }
            public int getColumnNumber() { return 34; }
            public int getCharacterOffset() { return 56; }
            public String getPublicId() { return "pubId"; }
            public String getSystemId() { return "sysId"; }
            public String toString() { return "L(12,34)"; }
        };
        check(l.getLineNumber() == 12, "getLineNumber");
        check(l.getColumnNumber() == 34, "getColumnNumber");
        check(l.getCharacterOffset() == 56, "getCharacterOffset");
        check("pubId".equals(l.getPublicId()), "getPublicId");
        check("sysId".equals(l.getSystemId()), "getSystemId");
        check("L(12,34)".equals(String.valueOf(l)), "toString override honored");
        // all-negative/-1 sentinel values are legal (used for unknown fields)
        javax.xml.stream.Location u = new javax.xml.stream.Location() {
            public int getLineNumber() { return -1; }
            public int getColumnNumber() { return -1; }
            public int getCharacterOffset() { return -1; }
            public String getPublicId() { return null; }
            public String getSystemId() { return null; }
        };
        check(u.getLineNumber() == -1 && u.getColumnNumber() == -1 && u.getCharacterOffset() == -1
            && u.getPublicId() == null && u.getSystemId() == null, "-1/null sentinels pass through");
    }

    // ---- Deepened 2: real parser Location values over a fixed multi-line document ----
    static void testRealParserLocations() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<r>\n  <a/>\n</r>"));
        r.next(); // START r
        javax.xml.stream.Location l0 = r.getLocation();
        check(l0.getLineNumber() == 1 && l0.getColumnNumber() == 4,
            "START r at line 1 col 4, got " + l0.getLineNumber() + "," + l0.getColumnNumber());
        r.next(); r.next(); // ws, START a
        javax.xml.stream.Location l1 = r.getLocation();
        check(l1.getLineNumber() == 2, "element a reported on line 2, got " + l1.getLineNumber());
        check(l1.getColumnNumber() >= 1, "column >= 1");
        check(l1.getCharacterOffset() == 10, "character offset 10, got " + l1.getCharacterOffset());
        check(l0.getPublicId() == null && l0.getSystemId() == null, "Reader input has no ids");
        String s = String.valueOf(l1);
        check(s.contains("Line number = 2") && s.contains("Column number = ")
            && s.contains("CharacterOffset"), "Location.toString exposes all fields");
        r.close();
        // parse error location precision
        try {
            javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader(
                "<r>\n  <a>\n</r>"));
            while (r2.hasNext()) r2.next();
            check(false, "unclosed element must throw");
        } catch (javax.xml.stream.XMLStreamException e) {
            check(e.getLocation().getLineNumber() == 3, "error location points at line 3, got "
                + e.getLocation().getLineNumber());
        }
    }
}
