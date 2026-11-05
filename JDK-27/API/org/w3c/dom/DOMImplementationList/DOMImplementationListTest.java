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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementationList contract (via registry) ----
    static void deepContract() throws Exception {
        org.w3c.dom.bootstrap.DOMImplementationRegistry reg = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
        org.w3c.dom.DOMImplementationList l = reg.getDOMImplementationList("Core 3.0");
        check(l != null, "getDOMImplementationList(Core 3.0) non-null");
        check(l.getLength() >= 1, "Core 3.0 list non-empty");
        check(l.item(0) != null, "item(0) is the first implementation");
        check(l.item(99) == null, "item(past end) null");
        check(l.item(-1) == null, "item(negative) null");
        boolean consistent = true;
        for (int i = 0; i < l.getLength(); i++) consistent &= (l.item(i) != null);
        check(consistent, "all in-range items resolve");
        org.w3c.dom.DOMImplementationList empty = reg.getDOMImplementationList("bogus 1.0");
        check(empty.getLength() == 0 && empty.item(0) == null, "unknown feature list is empty");
        // every listed implementation really supports the requested feature
        boolean allSupport = true;
        for (int i = 0; i < l.getLength(); i++) allSupport &= l.item(i).hasFeature("Core", "3.0");
        check(allSupport, "listed implementations satisfy the queried feature");
        // lists are snapshots: independent queries give equal-length results
        check(reg.getDOMImplementationList("Core 3.0").getLength() == l.getLength(), "repeated query is deterministic");
        // XML and LS feature lists also resolve
        check(reg.getDOMImplementationList("XML 3.0").getLength() >= 1, "XML 3.0 list non-empty");
    }
}
