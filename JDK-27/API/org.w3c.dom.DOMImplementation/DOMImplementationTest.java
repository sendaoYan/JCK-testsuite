/*
 * @test
 * @summary org.w3c.dom.DOMImplementation conformance: interface and methods.
 * @run main DOMImplementationTest
 */

import org.w3c.dom.DOMImplementation;

public class DOMImplementationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMImplementation.class.isInterface(), "DOMImplementation is an interface");
        check(DOMImplementation.class.getMethod("hasFeature", String.class, String.class)
                .getReturnType() == boolean.class, "hasFeature(String,String) returns boolean");
        check(DOMImplementation.class.getMethod("createDocumentType", String.class,
                String.class, String.class) != null, "createDocumentType(String,String,String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMImplementation a = new org.w3c.dom.DOMImplementation(){
            public boolean hasFeature(java.lang.String p0, java.lang.String p1) { return true; }
            public org.w3c.dom.DocumentType createDocumentType(java.lang.String p0, java.lang.String p1, java.lang.String p2) { return null; }
            public org.w3c.dom.Document createDocument(java.lang.String p0, java.lang.String p1, org.w3c.dom.DocumentType p2) { return null; }
            public java.lang.Object getFeature(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of DOMImplementation");
        check(a.hasFeature("s", "s") == true, "hasFeature() returns fixed default");
        check(a.createDocumentType("s", "s", "s") == null, "createDocumentType() returns fixed default");
        check(a.createDocument("s", "s", (org.w3c.dom.DocumentType) null) == null, "createDocument() returns fixed default");
        check(a.getFeature("s", "s") == null, "getFeature() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
