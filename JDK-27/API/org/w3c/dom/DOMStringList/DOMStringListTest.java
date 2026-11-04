/*
 * @test
 * @summary org.w3c.dom.DOMStringList conformance: interface and methods.
 * @run main DOMStringListTest
 */

import org.w3c.dom.DOMStringList;

public class DOMStringListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMStringList.class.isInterface(), "DOMStringList is an interface");
        check(DOMStringList.class.getMethod("item", int.class)
                .getReturnType() == String.class, "item(int) returns String");
        check(DOMStringList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(DOMStringList.class.getMethod("contains", String.class)
                .getReturnType() == boolean.class, "contains(String) returns boolean");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMStringList a = new org.w3c.dom.DOMStringList(){
            public java.lang.String item(int p0) { return "s"; }
            public int getLength() { return 3; }
            public boolean contains(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of DOMStringList");
        check("s".equals(a.item(3)), "item() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.contains("s") == true, "contains() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
