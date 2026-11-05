/*
 * @test
 * @summary javax.xml.transform.Result conformance: interface hierarchy and methods.
 * @run main ResultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ResultTest {
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
        Class<?> c = Class.forName("javax.xml.transform.Result");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSystemId"), "has method setSystemId");
        check(names.contains("getSystemId"), "has method getSystemId");
        testPiConstants();
        testHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: PI constants + implementor hierarchy
    static void testPiConstants() {
        check("javax.xml.transform.disable-output-escaping"
                  .equals(javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING),
              "PI_DISABLE_OUTPUT_ESCAPING constant");
        check("javax.xml.transform.enable-output-escaping"
                  .equals(javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING),
              "PI_ENABLE_OUTPUT_ESCAPING constant");
    }

    static void testHierarchy() {
        check(new javax.xml.transform.stream.StreamResult() instanceof javax.xml.transform.Result,
              "StreamResult implements Result");
        check(new javax.xml.transform.dom.DOMResult() instanceof javax.xml.transform.Result,
              "DOMResult implements Result");
        check(new javax.xml.transform.sax.SAXResult() instanceof javax.xml.transform.Result,
              "SAXResult implements Result");
        javax.xml.transform.Result r = new javax.xml.transform.Result() {
            String sid;
            public void setSystemId(String id) { sid = id; }
            public String getSystemId() { return sid; }
        };
        r.setSystemId("http://base/out.xml");
        check("http://base/out.xml".equals(r.getSystemId()),
              "setSystemId/getSystemId round-trip on custom impl");
    }
}
