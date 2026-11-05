/*
 * @test
 * @summary org.w3c.dom.traversal.DocumentTraversal conformance: reflection shape checks.
 * @run main DocumentTraversalTest
 */

import java.lang.reflect.*;

public class DocumentTraversalTest {
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
        Class<?> c = Class.forName("org.w3c.dom.traversal.DocumentTraversal");
        check(c != null, "org.w3c.dom.traversal.DocumentTraversal loads");
        check(c.getName().equals("org.w3c.dom.traversal.DocumentTraversal"), "binary name is org.w3c.dom.traversal.DocumentTraversal");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.traversal.DocumentTraversal is public");
        check(c.isInterface(), "org.w3c.dom.traversal.DocumentTraversal is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createNodeIterator"), "declares createNodeIterator");
        check(declares(c, "createTreeWalker"), "declares createTreeWalker");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentTraversal contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.traversal.DocumentTraversal dt = (org.w3c.dom.traversal.DocumentTraversal) d;
        check(dt != null, "Document casts to DocumentTraversal");
        org.w3c.dom.Element r = d.createElement("r");
        d.appendChild(r);
        r.appendChild(d.createElement("c"));
        org.w3c.dom.traversal.NodeIterator it = dt.createNodeIterator(r, -1, null, false);
        check(it != null && it.getRoot() == r, "createNodeIterator binds the given root");
        org.w3c.dom.traversal.TreeWalker tw = dt.createTreeWalker(r, -1, null, false);
        check(tw != null && tw.getRoot() == r, "createTreeWalker binds the given root");
        // expandEntityReferences is a plain boolean parameter (4th)
        org.w3c.dom.traversal.NodeIterator it2 = dt.createNodeIterator(r, org.w3c.dom.traversal.NodeFilter.SHOW_ALL, null, true);
        check(it2 != null, "expandEntityReferences flag accepted");
        // whatToShow is honored at creation time
        org.w3c.dom.traversal.NodeIterator onlyText = dt.createNodeIterator(r, org.w3c.dom.traversal.NodeFilter.SHOW_TEXT, null, false);
        org.w3c.dom.Node n;
        int textSeen = 0;
        while ((n = onlyText.nextNode()) != null) textSeen += (n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? 1 : 0);
        check(textSeen == 0, "whatToShow SHOW_TEXT over an element-only tree yields nothing");
        r.appendChild(d.createTextNode("x"));
        org.w3c.dom.traversal.NodeIterator onlyText2 = dt.createNodeIterator(r, org.w3c.dom.traversal.NodeFilter.SHOW_TEXT, null, false);
        check(onlyText2.nextNode() != null && onlyText2.nextNode() == null, "SHOW_TEXT iterator sees only the text node");
        // each document supports its own traversal factory
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.traversal.DocumentTraversal dt2 = (org.w3c.dom.traversal.DocumentTraversal) d2;
        check(dt2 != dt || dt2.equals(d), "second document also casts (per-document factories)");
        // iterators/walkers can be created from any subtree root, not only the document
        org.w3c.dom.Element sub = d.createElement("sub");
        r.appendChild(sub);
        org.w3c.dom.traversal.TreeWalker subWalker = dt.createTreeWalker(sub, -1, null, false);
        check(subWalker.nextNode() == null, "walker rooted at a leaf visits nothing below");
    }
}
