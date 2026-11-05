/*
 * @test
 * @summary org.w3c.dom.traversal.TreeWalker conformance: interface and methods.
 * @run main TreeWalkerTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.traversal.TreeWalker;

public class TreeWalkerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(TreeWalker.class.isInterface(), "TreeWalker is an interface");
        check(TreeWalker.class.getMethod("getRoot").getReturnType() == Node.class,
              "getRoot() returns Node");
        check(TreeWalker.class.getMethod("getCurrentNode").getReturnType() == Node.class,
              "getCurrentNode() returns Node");
        check(TreeWalker.class.getMethod("nextNode").getReturnType() == Node.class,
              "nextNode() returns Node");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.traversal.TreeWalker a = new org.w3c.dom.traversal.TreeWalker(){
            public org.w3c.dom.Node getRoot() { return null; }
            public int getWhatToShow() { return 3; }
            public org.w3c.dom.traversal.NodeFilter getFilter() { return null; }
            public boolean getExpandEntityReferences() { return true; }
            public org.w3c.dom.Node getCurrentNode() { return null; }
            public void setCurrentNode(org.w3c.dom.Node p0) { }
            public org.w3c.dom.Node parentNode() { return null; }
            public org.w3c.dom.Node firstChild() { return null; }
            public org.w3c.dom.Node lastChild() { return null; }
            public org.w3c.dom.Node previousSibling() { return null; }
            public org.w3c.dom.Node nextSibling() { return null; }
            public org.w3c.dom.Node previousNode() { return null; }
            public org.w3c.dom.Node nextNode() { return null; }
        };
        check(a != null, "anon impl of TreeWalker");
        check(a.getRoot() == null, "getRoot() returns fixed default");
        check(a.getWhatToShow() == 3, "getWhatToShow() returns fixed default");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        check(a.getExpandEntityReferences() == true, "getExpandEntityReferences() returns fixed default");
        check(a.getCurrentNode() == null, "getCurrentNode() returns fixed default");
        a.setCurrentNode((org.w3c.dom.Node) null);
        check(a.parentNode() == null, "parentNode() returns fixed default");
        check(a.firstChild() == null, "firstChild() returns fixed default");
        check(a.lastChild() == null, "lastChild() returns fixed default");
        check(a.previousSibling() == null, "previousSibling() returns fixed default");
        check(a.nextSibling() == null, "nextSibling() returns fixed default");
        check(a.previousNode() == null, "previousNode() returns fixed default");
        check(a.nextNode() == null, "nextNode() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral TreeWalker contract (navigation/SKIP-vs-REJECT/root bounds) ----
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
        org.w3c.dom.traversal.TreeWalker tw = dt.createTreeWalker(r, -1, null, false);
        check(tw.getRoot() == r && tw.getCurrentNode() == r, "root/currentNode start at the root");
        check(tw.firstChild() == a, "firstChild descends");
        check(tw.firstChild() == a.getFirstChild(), "firstChild again reaches text");
        check(tw.parentNode() == a, "parentNode ascends");
        check(tw.nextSibling() == b, "nextSibling moves across siblings");
        check(tw.previousSibling() == a, "previousSibling moves back");
        check(tw.lastChild() == a.getFirstChild(), "lastChild descends to the current node's last child");
        // navigation beyond the root yields null without throwing
        tw.setCurrentNode(r);
        check(tw.parentNode() == null, "parentNode above root -> null");
        check(tw.nextSibling() == null && tw.previousSibling() == null, "siblings above root -> null");
        check(tw.firstChild() == a, "walking still works after root-boundary probes");
        // setCurrentNode jumps anywhere in the subtree
        tw.setCurrentNode(b);
        check(tw.getCurrentNode() == b, "setCurrentNode repositions");
        check(tw.parentNode() == r, "navigation from repositioned current node");
        // SKIP skips the node but keeps its subtree reachable; REJECT prunes the subtree (核心差异)
        final int[] mode = new int[]{org.w3c.dom.traversal.NodeFilter.FILTER_SKIP};
        final java.util.List<String> seen = new java.util.ArrayList<String>();
        org.w3c.dom.traversal.TreeWalker skipWalker = dt.createTreeWalker(r, -1, new org.w3c.dom.traversal.NodeFilter() {
            public short acceptNode(org.w3c.dom.Node n) { seen.add(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? n.getNodeValue() : n.getNodeName()); return (short) mode[0]; }
        }, false);
        int visitedSkip = 0;
        while (skipWalker.nextNode() != null) visitedSkip++;
        check(visitedSkip == 0, "SKIP-all walker returns no nodes");
        check(seen.contains("a") && seen.contains("A") && seen.contains("b") && seen.contains("B"), "SKIP still visits children (subtree kept)");
        seen.clear();
        mode[0] = org.w3c.dom.traversal.NodeFilter.FILTER_REJECT;
        org.w3c.dom.traversal.TreeWalker rejWalker = dt.createTreeWalker(r, -1, new org.w3c.dom.traversal.NodeFilter() {
            public short acceptNode(org.w3c.dom.Node n) { seen.add(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? n.getNodeValue() : n.getNodeName()); return (short) mode[0]; }
        }, false);
        int visitedRej = 0;
        while (rejWalker.nextNode() != null) visitedRej++;
        check(visitedRej == 0, "REJECT-all walker returns no nodes");
        check(seen.contains("a") && seen.contains("b") && !seen.contains("A") && !seen.contains("B"), "REJECT prunes entire subtrees (children never presented)");
        // mixed: reject only element 'a' -> its text never seen
        seen.clear();
        org.w3c.dom.traversal.TreeWalker mixed = dt.createTreeWalker(r, -1, new org.w3c.dom.traversal.NodeFilter() {
            public short acceptNode(org.w3c.dom.Node n) { seen.add(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? n.getNodeValue() : n.getNodeName()); return "a".equals(n.getNodeName()) ? org.w3c.dom.traversal.NodeFilter.FILTER_REJECT : org.w3c.dom.traversal.NodeFilter.FILTER_ACCEPT; }
        }, false);
        int visitedMixed = 0;
        while (mixed.nextNode() != null) visitedMixed++;
        check(visitedMixed == 2, "rejecting 'a' leaves b + its text");
        check(!seen.contains("A"), "text under rejected element never presented");
        // whatToShow on the walker
        org.w3c.dom.traversal.TreeWalker onlyB = dt.createTreeWalker(r, 1 << 1, null, false);
        check(onlyB.firstChild() == null, "SHOW_ATTRIBUTE walker finds nothing in this tree");
    }
}
