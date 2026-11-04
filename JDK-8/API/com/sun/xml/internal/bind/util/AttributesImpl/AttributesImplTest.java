/*
 * @test
 * @summary com.sun.xml.internal.bind.util.AttributesImpl conformance: value class behavior.
 * @run main AttributesImplTest
 */

public class AttributesImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.bind.util.AttributesImpl v = new com.sun.xml.internal.bind.util.AttributesImpl();
        v.addAttribute("uri", "local", "q", "type", "value");
        check(v.getLength() == 1, "getLength");
        check("value".equals(v.getValue(0)), "getValue");
        check("local".equals(v.getLocalName(0)), "getLocalName");
        check("uri".equals(v.getURI(0)), "getURI");
        check("type".equals(v.getType(0)), "getType");
        v.clear();
        check(v.getLength() == 0, "clear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
