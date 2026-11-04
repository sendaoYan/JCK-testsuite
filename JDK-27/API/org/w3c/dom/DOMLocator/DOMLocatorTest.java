/*
 * @test
 * @summary org.w3c.dom.DOMLocator conformance: interface and methods.
 * @run main DOMLocatorTest
 */

import org.w3c.dom.DOMLocator;

public class DOMLocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMLocator.class.isInterface(), "DOMLocator is an interface");
        check(DOMLocator.class.getMethod("getLineNumber").getReturnType() == int.class,
              "getLineNumber() returns int");
        check(DOMLocator.class.getMethod("getColumnNumber").getReturnType() == int.class,
              "getColumnNumber() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMLocator a = new org.w3c.dom.DOMLocator(){
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
            public int getByteOffset() { return 3; }
            public int getUtf16Offset() { return 3; }
            public org.w3c.dom.Node getRelatedNode() { return null; }
            public java.lang.String getUri() { return "s"; }
        };
        check(a != null, "anon impl of DOMLocator");
        check(a.getLineNumber() == 3, "getLineNumber() returns fixed default");
        check(a.getColumnNumber() == 3, "getColumnNumber() returns fixed default");
        check(a.getByteOffset() == 3, "getByteOffset() returns fixed default");
        check(a.getUtf16Offset() == 3, "getUtf16Offset() returns fixed default");
        check(a.getRelatedNode() == null, "getRelatedNode() returns fixed default");
        check("s".equals(a.getUri()), "getUri() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
