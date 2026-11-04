/*
 * @test
 * @summary org.w3c.dom.DOMConfiguration conformance: interface and methods.
 * @run main DOMConfigurationTest
 */

import org.w3c.dom.DOMConfiguration;

public class DOMConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMConfiguration.class.isInterface(), "DOMConfiguration is an interface");
        check(DOMConfiguration.class.getMethod("setParameter", String.class, Object.class)
                != null, "setParameter(String,Object)");
        check(DOMConfiguration.class.getMethod("getParameter", String.class) != null,
              "getParameter(String)");
        check(DOMConfiguration.class.getMethod("canSetParameter", String.class, Object.class)
                .getReturnType() == boolean.class, "canSetParameter returns boolean");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMConfiguration a = new org.w3c.dom.DOMConfiguration(){
            public void setParameter(java.lang.String p0, java.lang.Object p1) { }
            public java.lang.Object getParameter(java.lang.String p0) { return null; }
            public boolean canSetParameter(java.lang.String p0, java.lang.Object p1) { return true; }
            public org.w3c.dom.DOMStringList getParameterNames() { return null; }
        };
        check(a != null, "anon impl of DOMConfiguration");
        a.setParameter("s", (java.lang.Object) null);
        check(a.getParameter("s") == null, "getParameter() returns fixed default");
        check(a.canSetParameter("s", (java.lang.Object) null) == true, "canSetParameter() returns fixed default");
        check(a.getParameterNames() == null, "getParameterNames() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
