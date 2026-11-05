/*
 * @test
 * @summary org.w3c.dom.traversal.NodeIterator conformance: reflection shape checks.
 * @run main NodeIteratorTest
 */

import java.lang.reflect.*;

public class NodeIteratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.traversal.NodeIterator");
        check(c != null, "org.w3c.dom.traversal.NodeIterator loads");
        check(c.getName().equals("org.w3c.dom.traversal.NodeIterator"), "binary name is org.w3c.dom.traversal.NodeIterator");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.traversal.NodeIterator is public");
        check(c.isInterface(), "org.w3c.dom.traversal.NodeIterator is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getRoot"), "declares getRoot");
        check(declares(c, "getWhatToShow"), "declares getWhatToShow");
        check(declares(c, "getFilter"), "declares getFilter");
        check(declares(c, "getExpandEntityReferences"), "declares getExpandEntityReferences");
        check(declares(c, "nextNode"), "declares nextNode");
        check(declares(c, "previousNode"), "declares previousNode");
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
