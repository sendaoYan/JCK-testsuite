/*
 * @test
 * @summary javax.xml.transform.SourceLocator conformance: interface hierarchy and methods.
 * @run main SourceLocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SourceLocatorTest {
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
        Class<?> c = Class.forName("javax.xml.transform.SourceLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPublicId"), "has method getPublicId");
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getLineNumber"), "has method getLineNumber");
        check(names.contains("getColumnNumber"), "has method getColumnNumber");
        testInterfaceSurface();
        testLocatorCarryingException();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: all-abstract surface + locator carried by TransformerException
    static void testInterfaceSurface() throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.SourceLocator");
        java.lang.reflect.Method[] ms = c.getMethods();
        check(ms.length == 4, "exactly 4 methods (publicId/systemId/line/column)");
        boolean allAbstract = true;
        for (java.lang.reflect.Method m : ms) {
            if (!java.lang.reflect.Modifier.isAbstract(m.getModifiers())) allAbstract = false;
        }
        check(allAbstract, "all SourceLocator methods abstract (no defaults)");
    }

    static void testLocatorCarryingException() {
        javax.xml.transform.SourceLocator loc = new javax.xml.transform.SourceLocator() {
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "http://base/x.xml"; }
            public int getLineNumber() { return 7; }
            public int getColumnNumber() { return 11; }
        };
        check("pub".equals(loc.getPublicId()) && "http://base/x.xml".equals(loc.getSystemId()),
              "locator getters");
        check(loc.getLineNumber() == 7 && loc.getColumnNumber() == 11, "line/column getters");
        javax.xml.transform.TransformerException e =
            new javax.xml.transform.TransformerException("m", loc);
        check(e.getLocator() instanceof javax.xml.transform.SourceLocator,
              "TransformerException carries its SourceLocator");
        String las = e.getLocationAsString();
        check(las != null && las.contains("7") && las.contains("11"),
              "getLocationAsString renders line/column");
        String mal = e.getMessageAndLocation();
        check(mal != null && mal.startsWith("m"), "getMessageAndLocation starts with message");
    }
}
