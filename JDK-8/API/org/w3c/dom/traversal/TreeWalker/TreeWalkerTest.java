/*
 * @test
 * @summary org.w3c.dom.traversal.TreeWalker conformance: reflection shape checks.
 * @run main TreeWalkerTest
 */

import java.lang.reflect.*;

public class TreeWalkerTest {
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
        Class<?> c = Class.forName("org.w3c.dom.traversal.TreeWalker");
        check(c != null, "org.w3c.dom.traversal.TreeWalker loads");
        check(c.getName().equals("org.w3c.dom.traversal.TreeWalker"), "binary name is org.w3c.dom.traversal.TreeWalker");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.traversal.TreeWalker is public");
        check(c.isInterface(), "org.w3c.dom.traversal.TreeWalker is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getRoot"), "declares getRoot");
        check(declares(c, "getWhatToShow"), "declares getWhatToShow");
        check(declares(c, "getFilter"), "declares getFilter");
        check(declares(c, "getExpandEntityReferences"), "declares getExpandEntityReferences");
        check(declares(c, "getCurrentNode"), "declares getCurrentNode");
        check(declares(c, "setCurrentNode"), "declares setCurrentNode");
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
