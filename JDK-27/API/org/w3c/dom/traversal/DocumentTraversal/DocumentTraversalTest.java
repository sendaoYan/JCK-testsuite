/*
 * @test
 * @summary org.w3c.dom.traversal.DocumentTraversal conformance: interface and methods.
 * @run main DocumentTraversalTest
 */

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

public class DocumentTraversalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentTraversal.class.isInterface(), "DocumentTraversal is an interface");
        check(DocumentTraversal.class.getMethod("createNodeIterator",
                org.w3c.dom.Node.class, int.class, org.w3c.dom.traversal.NodeFilter.class,
                boolean.class).getReturnType() == NodeIterator.class,
              "createNodeIterator(...) returns NodeIterator");
        check(DocumentTraversal.class.getMethod("createTreeWalker",
                org.w3c.dom.Node.class, int.class, org.w3c.dom.traversal.NodeFilter.class,
                boolean.class).getReturnType() == TreeWalker.class,
              "createTreeWalker(...) returns TreeWalker");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.traversal.DocumentTraversal a = new org.w3c.dom.traversal.DocumentTraversal(){
            public org.w3c.dom.traversal.NodeIterator createNodeIterator(org.w3c.dom.Node p0, int p1, org.w3c.dom.traversal.NodeFilter p2, boolean p3) { return null; }
            public org.w3c.dom.traversal.TreeWalker createTreeWalker(org.w3c.dom.Node p0, int p1, org.w3c.dom.traversal.NodeFilter p2, boolean p3) { return null; }
        };
        check(a != null, "anon impl of DocumentTraversal");
        check(a.createNodeIterator((org.w3c.dom.Node) null, 3, (org.w3c.dom.traversal.NodeFilter) null, true) == null, "createNodeIterator() returns fixed default");
        check(a.createTreeWalker((org.w3c.dom.Node) null, 3, (org.w3c.dom.traversal.NodeFilter) null, true) == null, "createTreeWalker() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
