/*
 * @test
 * @summary javax.xml.transform.Source conformance: interface hierarchy and methods.
 * @run main SourceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SourceTest {
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
        Class<?> c = Class.forName("javax.xml.transform.Source");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSystemId"), "has method setSystemId");
        check(names.contains("getSystemId"), "has method getSystemId");
        testHierarchy();
        testSystemIdContract();
        testJdk8Surface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: implementor hierarchy + systemId contract + JDK surface
    static void testHierarchy() {
        check(new javax.xml.transform.stream.StreamSource() instanceof javax.xml.transform.Source,
              "StreamSource implements Source");
        check(new javax.xml.transform.dom.DOMSource() instanceof javax.xml.transform.Source,
              "DOMSource implements Source");
        check(new javax.xml.transform.sax.SAXSource() instanceof javax.xml.transform.Source,
              "SAXSource implements Source");
    }

    static void testSystemIdContract() {
        javax.xml.transform.Source s = new javax.xml.transform.Source() {
            String sid;
            public void setSystemId(String id) { sid = id; }
            public String getSystemId() { return sid; }
        };
        check(s.getSystemId() == null, "fresh anonymous Source: systemId null");
        s.setSystemId("http://base/doc.xml");
        check("http://base/doc.xml".equals(s.getSystemId()),
              "setSystemId/getSystemId round-trip on custom impl");
        s.setSystemId(null);
        check(s.getSystemId() == null, "setSystemId(null) stored as null");
    }

    static void testJdk8Surface() throws Exception {
        // JDK-8 Source has no isEmpty() default method (added in later JDKs); lock absence
        // across the interface and its standard implementations.
        Class<?>[] cs = {
            javax.xml.transform.Source.class,
            javax.xml.transform.stream.StreamSource.class,
            javax.xml.transform.dom.DOMSource.class,
            javax.xml.transform.sax.SAXSource.class,
        };
        boolean allAbsent = true;
        for (Class<?> c : cs) {
            try { c.getMethod("isEmpty"); allAbsent = false; }
            catch (NoSuchMethodException e) { /* expected on JDK-8 */ }
        }
        check(allAbsent, "JDK-8: Source family has no isEmpty() method");
    }
}
