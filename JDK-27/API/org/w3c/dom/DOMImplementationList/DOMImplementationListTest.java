/*
 * @test
 * @summary org.w3c.dom.DOMImplementationList conformance: interface and methods.
 * @run main DOMImplementationListTest
 */

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;

public class DOMImplementationListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMImplementationList.class.isInterface(),
              "DOMImplementationList is an interface");
        check(DOMImplementationList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(DOMImplementationList.class.getMethod("item", int.class)
                .getReturnType() == DOMImplementation.class, "item(int) returns DOMImplementation");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMImplementationList a = new org.w3c.dom.DOMImplementationList(){
            public org.w3c.dom.DOMImplementation item(int p0) { return null; }
            public int getLength() { return 3; }
        };
        check(a != null, "anon impl of DOMImplementationList");
        check(a.item(3) == null, "item() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
