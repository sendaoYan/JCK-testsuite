/*
 * @test
 * @summary org.w3c.dom.NameList conformance: interface and methods.
 * @run main NameListTest
 */

import org.w3c.dom.NameList;

public class NameListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NameList.class.isInterface(), "NameList is an interface");
        check(NameList.class.getMethod("getName", int.class)
                .getReturnType() == String.class, "getName(int) returns String");
        check(NameList.class.getMethod("getNamespaceURI", int.class)
                .getReturnType() == String.class, "getNamespaceURI(int) returns String");
        check(NameList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.NameList a = new org.w3c.dom.NameList(){
            public java.lang.String getName(int p0) { return "s"; }
            public java.lang.String getNamespaceURI(int p0) { return "s"; }
            public int getLength() { return 3; }
            public boolean contains(java.lang.String p0) { return true; }
            public boolean containsNS(java.lang.String p0, java.lang.String p1) { return true; }
        };
        check(a != null, "anon impl of NameList");
        check("s".equals(a.getName(3)), "getName() returns fixed default");
        check("s".equals(a.getNamespaceURI(3)), "getNamespaceURI() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.contains("s") == true, "contains() returns fixed default");
        check(a.containsNS("s", "s") == true, "containsNS() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
