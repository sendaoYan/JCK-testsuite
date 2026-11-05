/*
 * @test
 * @summary org.w3c.dom.NodeList conformance: interface and methods.
 * @run main NodeListTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NodeList.class.isInterface(), "NodeList is an interface");
        check(NodeList.class.getMethod("item", int.class).getReturnType() == Node.class,
              "item(int) returns Node");
        check(NodeList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.NodeList a = new org.w3c.dom.NodeList(){
            public org.w3c.dom.Node item(int p0) { return null; }
            public int getLength() { return 3; }
        };
        check(a != null, "anon impl of NodeList");
        check(a.item(3) == null, "item() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }


    // ---- deepening round 2: behavioral NodeList contract (live view, OOB, wildcard scan) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = d.createElement("root");
        d.appendChild(r);
        org.w3c.dom.Element a = d.createElement("a");
        org.w3c.dom.Element b = d.createElement("b");
        r.appendChild(a);
        r.appendChild(b);

        // live view: mutations after obtaining the list are reflected
        org.w3c.dom.NodeList live = r.getChildNodes();
        check(live.getLength() == 2, "childNodes initial length");
        r.appendChild(d.createElement("c"));
        check(live.getLength() == 3, "childNodes reflects appends (live)");
        live.item(2).getParentNode().removeChild(live.item(2));
        check(live.getLength() == 2, "childNodes reflects removals (live)");
        // item bounds
        check(live.item(0) == a && live.item(1) == b, "item(i) returns children in order");
        check(live.item(2) == null, "item(past end) null");
        check(live.item(-1) == null, "item(negative) null");
        check(live.item(9999) == null, "item(far past end) null");

        // getElementsByTagName returns a live, recursive list
        org.w3c.dom.Element nested = d.createElement("a");
        b.appendChild(nested);
        org.w3c.dom.NodeList byTag = r.getElementsByTagName("a");
        check(byTag.getLength() == 2, "getElementsByTagName recursive match");
        r.appendChild(d.createElement("a"));
        check(byTag.getLength() == 3, "getElementsByTagName live on append");
        // wildcard
        org.w3c.dom.NodeList star = r.getElementsByTagName("*");
        check(star.getLength() == 4, "element-level wildcard excludes self");
        boolean selfAbsent = true;
        for (int i = 0; i < star.getLength(); i++) if (star.item(i) == r) selfAbsent = false;
        check(selfAbsent, "element wildcard never returns the context element");
        org.w3c.dom.NodeList docStar = d.getElementsByTagName("*");
        boolean rootIncluded = false;
        for (int i = 0; i < docStar.getLength(); i++) if (docStar.item(i) == r) rootIncluded = true;
        check(rootIncluded, "document wildcard includes documentElement");
        check(d.getElementsByTagName("a").getLength() == 3, "document-level tag scan");
        // removal reflected through old reference
        b.removeChild(nested);
        check(byTag.getLength() == 2, "getElementsByTagName live on removal");
        // namespaced scan
        org.w3c.dom.Element nsChild = d.createElementNS("urn:x", "p:a");
        r.appendChild(nsChild);
        check(r.getElementsByTagName("a").getLength() == 2, "non-ns tag scan does not match ns element");
        check(r.getElementsByTagNameNS("urn:x", "a").getLength() == 1, "ns scan matches ns element");
        check(r.getElementsByTagNameNS("*", "a").getLength() == 1, "ns wildcard-local only matches ns nodes (Xerces)");
        check(r.getElementsByTagNameNS("*", "*").getLength() == 4, "full wildcard ns scan");
        // empty result list
        org.w3c.dom.NodeList none = r.getElementsByTagName("zzz");
        check(none.getLength() == 0 && none.item(0) == null, "no-match list empty");
    }

}
