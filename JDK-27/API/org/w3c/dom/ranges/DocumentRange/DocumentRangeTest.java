/*
 * @test
 * @summary org.w3c.dom.ranges.DocumentRange conformance: interface and createRange.
 * @run main DocumentRangeTest
 */

import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;

public class DocumentRangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentRange.class.isInterface(), "DocumentRange is an interface");
        check(DocumentRange.class.getMethod("createRange")
                .getReturnType() == Range.class, "createRange() returns Range");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ranges.DocumentRange a = new org.w3c.dom.ranges.DocumentRange(){
            public org.w3c.dom.ranges.Range createRange() { return null; }
        };
        check(a != null, "anon impl of DocumentRange");
        check(a.createRange() == null, "createRange() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentRange contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ranges.DocumentRange dr = (org.w3c.dom.ranges.DocumentRange) d;
        check(dr != null, "Document casts to DocumentRange");
        org.w3c.dom.ranges.Range r1 = dr.createRange();
        check(r1 != null, "createRange non-null");
        check(r1.getCollapsed(), "fresh range is collapsed");
        check(r1.getStartContainer() == d && r1.getEndContainer() == d, "fresh range anchored at the document");
        check(r1.getStartOffset() == 0 && r1.getEndOffset() == 0, "fresh range offsets 0/0");
        org.w3c.dom.ranges.Range r2 = dr.createRange();
        check(r1 != r2, "createRange returns independent objects");
        r2.setStart(d, 0);
        check(r1.getCollapsed(), "mutating one range does not affect the other");
        // ranges stay usable after tree growth
        org.w3c.dom.Element e = d.createElement("r");
        d.appendChild(e);
        check(r1.getStartContainer() == d, "range container unchanged by appends");
        // createRange on a second document is independent of the first
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ranges.DocumentRange dr2 = (org.w3c.dom.ranges.DocumentRange) d2;
        org.w3c.dom.ranges.Range other = dr2.createRange();
        check(other.getStartContainer() == d2, "second document produces its own ranges");
    }
}
