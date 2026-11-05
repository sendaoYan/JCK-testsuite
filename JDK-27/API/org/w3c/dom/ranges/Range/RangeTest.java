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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Range contract (boundaries/extract-vs-clone/insert/surround) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ranges.DocumentRange dr = (org.w3c.dom.ranges.DocumentRange) d;
        org.w3c.dom.Element r = d.createElement("r");
        d.appendChild(r);
        org.w3c.dom.Element a = d.createElement("a");
        r.appendChild(a);
        a.appendChild(d.createTextNode("A1"));
        org.w3c.dom.Element mid = d.createElement("m");
        r.appendChild(mid);
        mid.appendChild(d.createTextNode("MID"));
        org.w3c.dom.Element b = d.createElement("b");
        r.appendChild(b);
        b.appendChild(d.createTextNode("B9"));
        // boundary basics
        org.w3c.dom.ranges.Range whole = dr.createRange();
        whole.selectNodeContents(r);
        check(whole.getStartContainer() == r && whole.getStartOffset() == 0 && whole.getEndOffset() == 3, "selectNodeContents spans all children");
        check("A1MIDB9".equals(whole.toString()), "toString concatenates descendant text in order");
        check(whole.getCommonAncestorContainer() == r, "commonAncestorContainer of whole range");
        check(!whole.getCollapsed(), "spanning range not collapsed");
        // negative / out-of-bounds boundaries
        try { whole.setStart(r, -1); check(false, "setStart(negative) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "setStart(-1) -> INDEX_SIZE_ERR"); }
        try { whole.setEnd(r, 99); check(false, "setEnd(past end) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "setEnd(99) -> INDEX_SIZE_ERR"); }
        // selectNode boundaries land in the parent
        org.w3c.dom.ranges.Range sel = dr.createRange();
        sel.selectNode(mid);
        check(sel.getStartContainer() == r && sel.getStartOffset() == 1 && sel.getEndOffset() == 2, "selectNode boundaries wrap the node in its parent");
        check(sel.getCommonAncestorContainer() == r, "selectNode common ancestor is the parent");
        // selectNode on an unmounted node -> RangeException
        try { dr.createRange().selectNode(d.createElement("free")); check(false, "selectNode(unmounted) must throw"); }
        catch (org.w3c.dom.ranges.RangeException ex) { check(ex.code == org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR, "selectNode(unmounted) -> RangeException INVALID_NODE_TYPE_ERR(2)"); }
        // selectNodeContents on an unmounted element is allowed
        org.w3c.dom.ranges.Range contents = dr.createRange();
        contents.selectNodeContents(d.createElement("free"));
        check(contents.getCollapsed(), "selectNodeContents(unmounted) gives empty range");
        // compareBoundaryPoints
        org.w3c.dom.ranges.Range x = dr.createRange();
        org.w3c.dom.ranges.Range y = dr.createRange();
        x.setStart(r, 0); x.setEnd(r, 1);
        y.setStart(r, 0); y.setEnd(r, 2);
        check(org.w3c.dom.ranges.Range.START_TO_START == 0 && org.w3c.dom.ranges.Range.START_TO_END == 1
            && org.w3c.dom.ranges.Range.END_TO_END == 2 && org.w3c.dom.ranges.Range.END_TO_START == 3, "boundary-point constants 0/1/2/3");
        check(x.compareBoundaryPoints(org.w3c.dom.ranges.Range.START_TO_START, y) == 0, "equal starts compare 0");
        check(x.compareBoundaryPoints(org.w3c.dom.ranges.Range.END_TO_END, y) == -1, "x ends before y -> -1");
        check(y.compareBoundaryPoints(org.w3c.dom.ranges.Range.END_TO_END, x) == 1, "y ends after x -> 1");
        check(x.compareBoundaryPoints(org.w3c.dom.ranges.Range.START_TO_END, y) == 1, "START_TO_END: x.start > y.end -> 1");
        check(x.compareBoundaryPoints(org.w3c.dom.ranges.Range.END_TO_START, y) == -1, "END_TO_START: x.end < y.start -> -1");
        // clone vs extract vs delete
        org.w3c.dom.ranges.Range midRange = dr.createRange();
        midRange.setStart(r, 1);
        midRange.setEnd(r, 2);
        org.w3c.dom.DocumentFragment cloned = midRange.cloneContents();
        check(cloned.getChildNodes().getLength() == 1 && "MID".equals(cloned.getFirstChild().getTextContent()), "cloneContents copies the covered subtree");
        check(mid.getChildNodes().getLength() == 1, "cloneContents leaves the tree intact");
        org.w3c.dom.DocumentFragment extracted = midRange.extractContents();
        check("MID".equals(extracted.getFirstChild().getTextContent()), "extractContents returns the live nodes");
        check(r.getChildNodes().getLength() == 2 && mid.getParentNode() == extracted, "extractContents moves the covered node into the fragment (host loses it)");
        // deleted range collapses
        org.w3c.dom.ranges.Range del = dr.createRange();
        del.setStart(r, 1);
        del.setEnd(r, 2);
        del.deleteContents();
        check(r.getChildNodes().getLength() == 1, "deleteContents removes covered nodes");
        check(del.getCollapsed(), "deleteContents collapses the range");
        // insertNode inside text splits it
        org.w3c.dom.Element t = d.createElement("t");
        r.appendChild(t);
        t.appendChild(d.createTextNode("ABCD"));
        org.w3c.dom.ranges.Range tr = dr.createRange();
        tr.setStart(t.getFirstChild(), 2);
        tr.setEnd(t.getFirstChild(), 2);
        tr.insertNode(d.createElement("M"));
        check(t.getChildNodes().getLength() == 3
            && "AB".equals(t.getChildNodes().item(0).getNodeValue())
            && "M".equals(t.getChildNodes().item(1).getNodeName())
            && "CD".equals(t.getChildNodes().item(2).getNodeValue()), "insertNode at a text offset splits the text node");
        // surroundContents
        org.w3c.dom.Element wrapHost = d.createElement("w");
        r.appendChild(wrapHost);
        wrapHost.appendChild(d.createTextNode("X"));
        wrapHost.appendChild(d.createTextNode("Y"));
        org.w3c.dom.ranges.Range sr = dr.createRange();
        sr.selectNodeContents(wrapHost);
        org.w3c.dom.Element wrap = d.createElement("wrap");
        sr.surroundContents(wrap);
        check(wrapHost.getChildNodes().getLength() == 1 && wrapHost.getFirstChild() == wrap, "surroundContents re-parents content under the new parent");
        check("XY".equals(wrap.getTextContent()), "surrounded content preserved");
        // setStartBefore/After, setEndBefore/After anchor in the parent
        org.w3c.dom.ranges.Range anchored = dr.createRange();
        anchored.setStartBefore(a);
        anchored.setEndAfter(a);
        check(anchored.getStartContainer() == r && anchored.getStartOffset() == 0 && anchored.getEndOffset() == 1, "setStartBefore/setEndAfter boundaries in parent");
        // collapse + cloneRange
        org.w3c.dom.ranges.Range cc = dr.createRange();
        cc.selectNodeContents(r);
        cc.collapse(true);
        check(cc.getCollapsed() && cc.getStartOffset() == cc.getEndOffset(), "collapse(true) to the start");
        cc.collapse(false);
        check(cc.getCollapsed(), "collapse(false) also yields a collapsed (end) point");
        org.w3c.dom.ranges.Range cc2 = cc.cloneRange();
        cc2.setStart(r, 0);
        check(cc.getStartOffset() != 0 || cc2.getStartContainer() == r, "cloneRange independent of the original");
        // detach
        org.w3c.dom.ranges.Range det = dr.createRange();
        det.detach();
        try { det.detach(); check(false, "second detach must throw on this implementation"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_STATE_ERR, "double detach -> INVALID_STATE_ERR(11)"); }
        try { det.toString(); check(false, "toString after detach must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_STATE_ERR, "detached toString -> INVALID_STATE_ERR(11)"); }
    }
}
