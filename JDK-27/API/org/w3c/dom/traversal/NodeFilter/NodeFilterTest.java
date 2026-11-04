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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
