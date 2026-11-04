/*
 * @test
 * @summary org.w3c.dom.DOMError conformance: interface and methods.
 * @run main DOMErrorTest
 */

import org.w3c.dom.DOMError;

public class DOMErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMError.class.isInterface(), "DOMError is an interface");
        check(DOMError.class.getMethod("getSeverity") != null, "getSeverity()");
        check(DOMError.class.getMethod("getMessage") != null, "getMessage()");
        check(DOMError.class.getMethod("getType") != null, "getType()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.DOMError.SEVERITY_WARNING == 1, "SEVERITY_WARNING == 1");
        check(org.w3c.dom.DOMError.SEVERITY_ERROR == 2, "SEVERITY_ERROR == 2");
        check(org.w3c.dom.DOMError.SEVERITY_FATAL_ERROR == 3, "SEVERITY_FATAL_ERROR == 3");
        org.w3c.dom.DOMError a = new org.w3c.dom.DOMError(){
            public short getSeverity() { return (short)2; }
            public java.lang.String getMessage() { return "s"; }
            public java.lang.String getType() { return "s"; }
            public java.lang.Object getRelatedException() { return null; }
            public java.lang.Object getRelatedData() { return null; }
            public org.w3c.dom.DOMLocator getLocation() { return null; }
        };
        check(a != null, "anon impl of DOMError");
        check(a.getSeverity() == (short)2, "getSeverity() returns fixed default");
        check("s".equals(a.getMessage()), "getMessage() returns fixed default");
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.getRelatedException() == null, "getRelatedException() returns fixed default");
        check(a.getRelatedData() == null, "getRelatedData() returns fixed default");
        check(a.getLocation() == null, "getLocation() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
