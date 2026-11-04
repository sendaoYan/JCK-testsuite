/*
 * @test
 * @summary org.w3c.dom.ranges.Range conformance: interface, compare constants.
 * @run main RangeTest
 */

import org.w3c.dom.ranges.Range;

public class RangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Range.class.isInterface(), "Range is an interface");
        check(Range.START_TO_START == 0, "START_TO_START == 0");
        check(Range.START_TO_END == 1, "START_TO_END == 1");
        check(Range.END_TO_END == 2, "END_TO_END == 2");
        check(Range.END_TO_START == 3, "END_TO_START == 3");
        check(Range.class.getMethod("getStartContainer") != null, "getStartContainer()");
        check(Range.class.getMethod("getEndContainer") != null, "getEndContainer()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.ranges.Range.START_TO_START == 0, "START_TO_START == 0");
        check(org.w3c.dom.ranges.Range.START_TO_END == 1, "START_TO_END == 1");
        check(org.w3c.dom.ranges.Range.END_TO_END == 2, "END_TO_END == 2");
        check(org.w3c.dom.ranges.Range.END_TO_START == 3, "END_TO_START == 3");
        org.w3c.dom.ranges.Range a = new org.w3c.dom.ranges.Range(){
            public org.w3c.dom.Node getStartContainer() { return null; }
            public int getStartOffset() { return 3; }
            public org.w3c.dom.Node getEndContainer() { return null; }
            public int getEndOffset() { return 3; }
            public boolean getCollapsed() { return true; }
            public org.w3c.dom.Node getCommonAncestorContainer() { return null; }
            public void setStart(org.w3c.dom.Node p0, int p1) { }
            public void setEnd(org.w3c.dom.Node p0, int p1) { }
            public void setStartBefore(org.w3c.dom.Node p0) { }
            public void setStartAfter(org.w3c.dom.Node p0) { }
            public void setEndBefore(org.w3c.dom.Node p0) { }
            public void setEndAfter(org.w3c.dom.Node p0) { }
            public void collapse(boolean p0) { }
            public void selectNode(org.w3c.dom.Node p0) { }
            public void selectNodeContents(org.w3c.dom.Node p0) { }
            public short compareBoundaryPoints(short p0, org.w3c.dom.ranges.Range p1) { return (short)2; }
            public void deleteContents() { }
            public org.w3c.dom.DocumentFragment extractContents() { return null; }
            public org.w3c.dom.DocumentFragment cloneContents() { return null; }
            public void insertNode(org.w3c.dom.Node p0) { }
            public void surroundContents(org.w3c.dom.Node p0) { }
            public org.w3c.dom.ranges.Range cloneRange() { return null; }
            public java.lang.String toString() { return "s"; }
            public void detach() { }
        };
        check(a != null, "anon impl of Range");
        check(a.getStartContainer() == null, "getStartContainer() returns fixed default");
        check(a.getStartOffset() == 3, "getStartOffset() returns fixed default");
        check(a.getEndContainer() == null, "getEndContainer() returns fixed default");
        check(a.getEndOffset() == 3, "getEndOffset() returns fixed default");
        check(a.getCollapsed() == true, "getCollapsed() returns fixed default");
        check(a.getCommonAncestorContainer() == null, "getCommonAncestorContainer() returns fixed default");
        a.setStart((org.w3c.dom.Node) null, 3);
        a.setEnd((org.w3c.dom.Node) null, 3);
        a.setStartBefore((org.w3c.dom.Node) null);
        a.setStartAfter((org.w3c.dom.Node) null);
        a.setEndBefore((org.w3c.dom.Node) null);
        a.setEndAfter((org.w3c.dom.Node) null);
        a.collapse(true);
        a.selectNode((org.w3c.dom.Node) null);
        a.selectNodeContents((org.w3c.dom.Node) null);
        check(a.compareBoundaryPoints((short)2, (org.w3c.dom.ranges.Range) null) == (short)2, "compareBoundaryPoints() returns fixed default");
        a.deleteContents();
        check(a.extractContents() == null, "extractContents() returns fixed default");
        check(a.cloneContents() == null, "cloneContents() returns fixed default");
        a.insertNode((org.w3c.dom.Node) null);
        a.surroundContents((org.w3c.dom.Node) null);
        check(a.cloneRange() == null, "cloneRange() returns fixed default");
        check("s".equals(a.toString()), "toString() returns fixed default");
        a.detach();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
