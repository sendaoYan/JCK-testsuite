/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataNode conformance: behavioral.
 * @run main IIOMetadataNodeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOMetadataNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.metadata.IIOMetadataNode n = new javax.imageio.metadata.IIOMetadataNode("root");
        check("root".equals(n.getNodeName()), "nodeName");
        check(n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE, "nodeType element");
        n.setAttribute("k", "v");
        check("v".equals(n.getAttribute("k")), "attribute");
        javax.imageio.metadata.IIOMetadataNode child = new javax.imageio.metadata.IIOMetadataNode("child");
        n.appendChild(child);
        check(n.getChildNodes().getLength() == 1, "child length");
        check(n.getFirstChild() == child, "firstChild");
        check(n.getLength() == 1, "nodelist length");
        check(n.item(0) == child, "item");
        check(n.getUserObject() == null, "userObject null");
        n.setUserObject("x");
        check("x".equals(n.getUserObject()), "setUserObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
