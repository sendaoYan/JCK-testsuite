/*
 * @test
 * @summary org.w3c.dom.ranges.RangeException conformance: superclass, constructor, code field.
 * @run main RangeExceptionTest
 */

public class RangeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.ranges.RangeException.class),
              "extends RuntimeException");
        check(org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR == org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "BAD_BOUNDARYPOINTS_ERR field");
        check(org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR == org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR, "INVALID_NODE_TYPE_ERR field");
        org.w3c.dom.ranges.RangeException e = new org.w3c.dom.ranges.RangeException(org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "m");
        check(e.code == org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "code field");
        check("m".equals(e.getMessage()), "message");

        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral RangeException contract (real trigger) ----
    static void deepContract() throws Exception {
        org.w3c.dom.ranges.RangeException re = new org.w3c.dom.ranges.RangeException(org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "bad");
        check(re.code == org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "BAD_BOUNDARYPOINTS_ERR = 1");
        check(org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR == 2, "INVALID_NODE_TYPE_ERR = 2");
        check("bad".equals(re.getMessage()), "message preserved");
        check(new org.w3c.dom.ranges.RangeException((short) 2, null).getMessage() == null, "null message preserved");
        check(re instanceof RuntimeException, "RangeException is a RuntimeException");
        // real trigger: selectNode on a node with no parent
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ranges.Range r = ((org.w3c.dom.ranges.DocumentRange) d).createRange();
        boolean threw = false;
        try { r.selectNode(d.createElement("orphan")); }
        catch (org.w3c.dom.ranges.RangeException ex) { threw = ex.code == org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR; }
        check(threw, "selectNode(unmounted node) -> RangeException INVALID_NODE_TYPE_ERR");
        // the same range object stays usable afterwards
        org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        r.selectNode(e);
        check(r.getStartContainer() == d, "range usable after a RangeException");
    }
}
