/*
 * @test
 * @summary org.w3c.dom.traversal.NodeFilter conformance: interface, constants, acceptNode.
 * @run main NodeFilterTest
 */

import org.w3c.dom.traversal.NodeFilter;

public class NodeFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NodeFilter.class.isInterface(), "NodeFilter is an interface");
        check(NodeFilter.FILTER_ACCEPT == 1, "FILTER_ACCEPT == 1");
        check(NodeFilter.FILTER_REJECT == 2, "FILTER_REJECT == 2");
        check(NodeFilter.FILTER_SKIP == 3, "FILTER_SKIP == 3");
        check(NodeFilter.class.getMethod("acceptNode", org.w3c.dom.Node.class)
                .getReturnType() == short.class, "acceptNode(Node) returns short");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.traversal.NodeFilter.FILTER_ACCEPT == 1, "FILTER_ACCEPT == 1");
        check(org.w3c.dom.traversal.NodeFilter.FILTER_REJECT == 2, "FILTER_REJECT == 2");
        check(org.w3c.dom.traversal.NodeFilter.FILTER_SKIP == 3, "FILTER_SKIP == 3");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ALL == -1, "SHOW_ALL == -1");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ELEMENT == 1, "SHOW_ELEMENT == 1");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ATTRIBUTE == 2, "SHOW_ATTRIBUTE == 2");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_TEXT == 4, "SHOW_TEXT == 4");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_CDATA_SECTION == 8, "SHOW_CDATA_SECTION == 8");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ENTITY_REFERENCE == 16, "SHOW_ENTITY_REFERENCE == 16");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ENTITY == 32, "SHOW_ENTITY == 32");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_PROCESSING_INSTRUCTION == 64, "SHOW_PROCESSING_INSTRUCTION == 64");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_COMMENT == 128, "SHOW_COMMENT == 128");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_DOCUMENT == 256, "SHOW_DOCUMENT == 256");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_DOCUMENT_TYPE == 512, "SHOW_DOCUMENT_TYPE == 512");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_DOCUMENT_FRAGMENT == 1024, "SHOW_DOCUMENT_FRAGMENT == 1024");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_NOTATION == 2048, "SHOW_NOTATION == 2048");
        org.w3c.dom.traversal.NodeFilter a = new org.w3c.dom.traversal.NodeFilter(){
            public short acceptNode(org.w3c.dom.Node p0) { return (short)2; }
        };
        check(a != null, "anon impl of NodeFilter");
        check(a.acceptNode((org.w3c.dom.Node) null) == (short)2, "acceptNode() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral NodeFilter contract (constants + decision semantics) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.traversal.NodeFilter.FILTER_ACCEPT == 1, "FILTER_ACCEPT = 1");
        check(org.w3c.dom.traversal.NodeFilter.FILTER_REJECT == 2, "FILTER_REJECT = 2");
        check(org.w3c.dom.traversal.NodeFilter.FILTER_SKIP == 3, "FILTER_SKIP = 3");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ALL == -1 && Integer.toHexString(org.w3c.dom.traversal.NodeFilter.SHOW_ALL).equals("ffffffff"), "SHOW_ALL = 0xFFFFFFFF (int -1)");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ELEMENT == 0x1, "SHOW_ELEMENT = 0x1");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ATTRIBUTE == 0x2, "SHOW_ATTRIBUTE = 0x2");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_TEXT == 0x4, "SHOW_TEXT = 0x4");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_CDATA_SECTION == 0x8, "SHOW_CDATA_SECTION = 0x8");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ENTITY_REFERENCE == 0x10, "SHOW_ENTITY_REFERENCE = 0x10");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_ENTITY == 0x20, "SHOW_ENTITY = 0x20");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_PROCESSING_INSTRUCTION == 0x40, "SHOW_PROCESSING_INSTRUCTION = 0x40");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_COMMENT == 0x80, "SHOW_COMMENT = 0x80");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_DOCUMENT == 0x100, "SHOW_DOCUMENT = 0x100");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_DOCUMENT_TYPE == 0x200, "SHOW_DOCUMENT_TYPE = 0x200");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_DOCUMENT_FRAGMENT == 0x400, "SHOW_DOCUMENT_FRAGMENT = 0x400");
        check(org.w3c.dom.traversal.NodeFilter.SHOW_NOTATION == 0x800, "SHOW_NOTATION = 0x800");
        // user filter is consulted for every candidate node with the node as argument
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = d.createElement("r");
        d.appendChild(r);
        r.appendChild(d.createElement("a"));
        r.appendChild(d.createTextNode("t"));
        final java.util.List<org.w3c.dom.Node> seen = new java.util.ArrayList<org.w3c.dom.Node>();
        org.w3c.dom.traversal.NodeFilter counter = new org.w3c.dom.traversal.NodeFilter() {
            public short acceptNode(org.w3c.dom.Node n) { seen.add(n); return org.w3c.dom.traversal.NodeFilter.FILTER_ACCEPT; }
        };
        org.w3c.dom.traversal.TreeWalker tw = ((org.w3c.dom.traversal.DocumentTraversal) d).createTreeWalker(r, -1, counter, false);
        int visited = 0;
        while (tw.nextNode() != null) visited++;
        check(visited == 2, "walker visits element + text");
        check(seen.size() == 3 && seen.get(2).getNodeName().equals("r"), "walker consults a, #text, then re-consults the root while seeking the next sibling");
        check(seen.get(0).getNodeName().equals("a") && seen.get(1).getNodeName().equals("#text"), "candidates presented in document order");
        // whatToShow and filter combine: mask first, filter second
        org.w3c.dom.traversal.NodeIterator it = ((org.w3c.dom.traversal.DocumentTraversal) d).createNodeIterator(r,
            org.w3c.dom.traversal.NodeFilter.SHOW_ELEMENT, counter, false);
        seen.clear();
        while (it.nextNode() != null) { }
        check(seen.size() == 2 && seen.get(0).getNodeName().equals("r") && seen.get(1).getNodeName().equals("a"), "whatToShow prunes text candidates before the filter runs (root still presented)");
    }
}
