/*
 * @test
 * @summary org.w3c.dom.traversal.NodeIterator conformance: interface and methods.
 * @run main NodeIteratorTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class NodeIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NodeIterator.class.isInterface(), "NodeIterator is an interface");
        check(NodeIterator.class.getMethod("getRoot").getReturnType() == Node.class,
              "getRoot() returns Node");
        check(NodeIterator.class.getMethod("getWhatToShow").getReturnType() == int.class,
              "getWhatToShow() returns int");
        check(NodeIterator.class.getMethod("nextNode").getReturnType() == Node.class,
              "nextNode() returns Node");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.traversal.NodeIterator a = new org.w3c.dom.traversal.NodeIterator(){
            public org.w3c.dom.Node getRoot() { return null; }
            public int getWhatToShow() { return 3; }
            public org.w3c.dom.traversal.NodeFilter getFilter() { return null; }
            public boolean getExpandEntityReferences() { return true; }
            public org.w3c.dom.Node nextNode() { return null; }
            public org.w3c.dom.Node previousNode() { return null; }
            public void detach() { }
        };
        check(a != null, "anon impl of NodeIterator");
        check(a.getRoot() == null, "getRoot() returns fixed default");
        check(a.getWhatToShow() == 3, "getWhatToShow() returns fixed default");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        check(a.getExpandEntityReferences() == true, "getExpandEntityReferences() returns fixed default");
        check(a.nextNode() == null, "nextNode() returns fixed default");
        check(a.previousNode() == null, "previousNode() returns fixed default");
        a.detach();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral NodeIterator contract (order/live/detach/SKIP-vs-REJECT) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.traversal.DocumentTraversal dt = (org.w3c.dom.traversal.DocumentTraversal) d;
        org.w3c.dom.Element r = d.createElement("r");
        d.appendChild(r);
        org.w3c.dom.Element a = d.createElement("a");
        r.appendChild(a);
        a.appendChild(d.createTextNode("A"));
        org.w3c.dom.Element b = d.createElement("b");
        r.appendChild(b);
        b.appendChild(d.createTextNode("B"));
        // document-order traversal with nextNode/previousNode
        org.w3c.dom.traversal.NodeIterator it = dt.createNodeIterator(r, -1, null, false);
        org.w3c.dom.Node n1 = it.nextNode();
        org.w3c.dom.Node n2 = it.nextNode();
        org.w3c.dom.Node n3 = it.nextNode();
        org.w3c.dom.Node n4 = it.nextNode();
        org.w3c.dom.Node n5 = it.nextNode();
        org.w3c.dom.Node n6 = it.nextNode();
        check(n1 == r && n2 == a && n3 == a.getFirstChild() && n4 == b && n5 == b.getFirstChild() && n6 == null, "nextNode walks in document order starting at the root and ends at null");
        check(it.previousNode() == b.getFirstChild(), "previousNode steps back after exhaustion");
        check(it.previousNode() == b, "previousNode steps back again");
        check(it.nextNode() == b, "nextNode after previousNode resumes");
        // previousNode before any nextNode yields null
        org.w3c.dom.traversal.NodeIterator it0 = dt.createNodeIterator(r, -1, null, false);
        check(it0.previousNode() == null, "previousNode before first nextNode -> null");
        // fresh iterator replays identically (no consumption side effects)
        org.w3c.dom.traversal.NodeIterator it2 = dt.createNodeIterator(r, -1, null, false);
        check(it2.nextNode() == r && it2.nextNode() == a, "second iterator replays the same sequence");
        // whatToShow mask
        org.w3c.dom.traversal.NodeIterator texts = dt.createNodeIterator(r, org.w3c.dom.traversal.NodeFilter.SHOW_TEXT, null, false);
        check(texts.nextNode() == a.getFirstChild() && texts.nextNode() == b.getFirstChild() && texts.nextNode() == null, "SHOW_TEXT yields only text nodes");
        // live view: appending is visible to an existing iterator
        org.w3c.dom.Element c = d.createElement("c");
        r.appendChild(c);
        check(it2.nextNode() == a.getFirstChild() && it2.nextNode() == b && it2.nextNode() == b.getFirstChild() && it2.nextNode() == c, "iterator observes later appends (live)");
        // SKIP and REJECT behave identically on iterators (node skipped, children still visited)
        final int[] mode = new int[]{org.w3c.dom.traversal.NodeFilter.FILTER_SKIP};
        final java.util.List<String> candidates = new java.util.ArrayList<String>();
        org.w3c.dom.traversal.NodeIterator skipIt = dt.createNodeIterator(r, -1, new org.w3c.dom.traversal.NodeFilter() {
            public short acceptNode(org.w3c.dom.Node n) { candidates.add(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? n.getNodeValue() : n.getNodeName()); return (short) mode[0]; }
        }, false);
        int visited = 0;
        while (skipIt.nextNode() != null) visited++;
        check(visited == 0, "SKIP on every candidate yields no nodes");
        check(candidates.contains("A") && candidates.contains("B"), "SKIP still descends into children");
        candidates.clear();
        mode[0] = org.w3c.dom.traversal.NodeFilter.FILTER_REJECT;
        org.w3c.dom.traversal.NodeIterator rejIt = dt.createNodeIterator(r, -1, new org.w3c.dom.traversal.NodeFilter() {
            public short acceptNode(org.w3c.dom.Node n) { candidates.add(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? n.getNodeValue() : n.getNodeName()); return (short) mode[0]; }
        }, false);
        int visitedR = 0;
        while (rejIt.nextNode() != null) visitedR++;
        check(visitedR == 0 && candidates.contains("A") && candidates.contains("B"), "REJECT is equivalent to SKIP on iterators (flat document order descends anyway; walkers differ)");
        // detach
        org.w3c.dom.traversal.NodeIterator det = dt.createNodeIterator(r, -1, null, false);
        det.nextNode();
        det.detach();
        det.detach();
        check(true, "detach is idempotent");
        try { det.nextNode(); check(false, "nextNode after detach must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_STATE_ERR, "detached nextNode -> INVALID_STATE_ERR(11)"); }
        // getRoot stays available after detach
        check(det.getRoot() == r, "getRoot still reports the root after detach");
    }
}
