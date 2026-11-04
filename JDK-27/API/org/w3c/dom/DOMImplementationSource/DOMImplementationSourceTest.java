/*
 * @test
 * @summary org.w3c.dom.DOMImplementationSource conformance: interface and methods.
 * @run main DOMImplementationSourceTest
 */

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

public class DOMImplementationSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMImplementationSource.class.isInterface(),
              "DOMImplementationSource is an interface");
        check(DOMImplementationSource.class.getMethod("getDOMImplementation", String.class)
                .getReturnType() == DOMImplementation.class,
              "getDOMImplementation(String) returns DOMImplementation");
        check(DOMImplementationSource.class.getMethod("getDOMImplementationList", String.class)
                .getReturnType() == DOMImplementationList.class,
              "getDOMImplementationList(String) returns DOMImplementationList");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMImplementationSource a = new org.w3c.dom.DOMImplementationSource(){
            public org.w3c.dom.DOMImplementation getDOMImplementation(java.lang.String p0) { return null; }
            public org.w3c.dom.DOMImplementationList getDOMImplementationList(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of DOMImplementationSource");
        check(a.getDOMImplementation("s") == null, "getDOMImplementation() returns fixed default");
        check(a.getDOMImplementationList("s") == null, "getDOMImplementationList() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
