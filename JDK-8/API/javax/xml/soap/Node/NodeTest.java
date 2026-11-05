/*
 * @test
 * @summary javax.xml.soap.Node conformance: SAAJ 节点生命周期(getValue/setValue 单文本子替换/detachNode/双 detach 不抛/setParentElement 再收养/recycleNode 语义)/真实消息通道父链/SAAJ-DOM 双视角.
 * @run main NodeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.soap.Node");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.w3c.dom.Node"), "implements org.w3c.dom.Node");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("setValue"), "has method setValue");
        check(names.contains("setParentElement"), "has method setParentElement");
        check(names.contains("getParentElement"), "has method getParentElement");
        check(names.contains("detachNode"), "has method detachNode");
        check(names.contains("recycleNode"), "has method recycleNode");
        check(c.getDeclaredMethods().length == 6, "exactly 6 own methods");
        testValueLifecycle();
        testDetachLifecycle();
        testSetParentElementReadopt();
        testRecycleNode();
        testHeaderChannel();
        testSaaJDomDualView();
        testNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPBody body() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody();
    }

    static int count(java.util.Iterator<?> it) {
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        return n;
    }

    // getValue/setValue：空元素 null；setValue 建单文本子，再 setValue 原地替换（子数恒 1）
    static void testValueLifecycle() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPBodyElement el = body.addBodyElement(
                new javax.xml.namespace.QName("urn:n", "vEl", "n"));
        check(el instanceof javax.xml.soap.Node, "SOAPBodyElement is a SAAJ Node");
        check(el.getValue() == null, "fresh element getValue null");
        el.setValue("v1");
        check("v1".equals(el.getValue()), "setValue/getValue round-trip");
        check("v1".equals(el.getTextContent()), "textContent after first setValue");
        check(count(el.getChildElements()) == 1, "setValue creates exactly one text child");
        el.setValue("v2");
        check("v2".equals(el.getValue()), "second setValue overwrites value");
        check("v2".equals(el.getTextContent()), "textContent after overwrite");
        check(count(el.getChildElements()) == 1, "overwrite keeps single text child (replace semantics)");
        // 相邻文本：addTextNode 追加为兄弟 Text 节点；RI 实测 getValue() 读取时会归并相邻文本（normalize-on-read）
        el.addTextNode("x");
        check(count(el.getChildElements()) == 2, "addTextNode adds a second sibling text node");
        check("v2x".equals(el.getValue()), "getValue concatenates adjacent text children");
        check(count(el.getChildElements()) == 1, "RI getValue() merges adjacent text nodes (2->1, locked)");
        check("v2x".equals(el.getTextContent()), "textContent keeps concatenated value after merge");
    }

    // detachNode：父计数归零 + SAAJ/DOM 父均 null + 双 detach 不抛
    static void testDetachLifecycle() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPBodyElement el = body.addBodyElement(
                new javax.xml.namespace.QName("urn:n", "dEl", "n"));
        check(el.getParentElement() == body, "SAAJ parent before detach");
        check(el.getParentNode() == body, "DOM parent before detach");
        el.detachNode();
        check(count(body.getChildElements()) == 0, "body child count 0 after detach");
        check(el.getParentElement() == null, "SAAJ parent null after detach");
        check(el.getParentNode() == null, "DOM parent null after detach");
        check(body.getElementsByTagNameNS("urn:n", "dEl").getLength() == 0, "DOM lookup empty after detach");
        el.detachNode();
        check(true, "second detachNode does not throw");
        // detach 后元素仍可独立 setValue
        el.setValue("detached");
        check("detached".equals(el.getValue()), "detached element still usable via setValue");
    }

    // setParentElement：detach 后重新收养（RI 实测真实挂回 DOM 子树）
    static void testSetParentElementReadopt() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPBodyElement el = body.addBodyElement(
                new javax.xml.namespace.QName("urn:n", "rEl", "n"));
        el.detachNode();
        check(count(body.getChildElements()) == 0, "pre-adopt count 0");
        el.setParentElement(body); // SAAJ Node.setParentElement 返回 void
        check(el.getParentElement() == body, "parent restored after setParentElement");
        check(count(body.getChildElements()) == 1, "RI re-adopts element into DOM child list (locked)");
        check(body.getElementsByTagNameNS("urn:n", "rEl").getLength() == 1, "re-adopted element DOM visible");
        // 父设置后原值保留
        el.setValue("adopted");
        check("adopted".equals(body.getTextContent().replace("\n", "").trim())
                        || body.getTextContent().contains("adopted"),
                "value readable through parent after re-adopt");
    }

    // recycleNode：等价 detach（父清空）+ 幂等不抛 + 节点仍可用（RI 不失效化）
    static void testRecycleNode() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPBodyElement first = body.addBodyElement(
                new javax.xml.namespace.QName("urn:n", "c1", "n"));
        javax.xml.soap.SOAPBodyElement second = body.addBodyElement(
                new javax.xml.namespace.QName("urn:n", "c2", "n"));
        check(count(body.getChildElements()) == 2, "two children before recycle");
        second.recycleNode();
        check(count(body.getChildElements()) == 1, "recycleNode detaches from parent (count 2->1)");
        check(second.getParentElement() == null, "recycled node parent null");
        second.recycleNode();
        check(true, "double recycleNode does not throw");
        second.setValue("post-recycle");
        check("post-recycle".equals(second.getValue()), "recycled node remains usable in RI");
        check(first.getParentElement() == body, "untouched sibling unaffected");
    }

    // header 通道：同样生命周期语义
    static void testHeaderChannel() throws Exception {
        javax.xml.soap.SOAPHeader header = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:n", "hEl", "n"));
        check(he.getParentElement() == header, "header element parent is header");
        he.setValue("hv");
        check("hv".equals(he.getValue()), "header element value round-trip");
        he.detachNode();
        check(count(header.getChildElements()) == 0, "header count 0 after detach");
        check(he.getParentNode() == null, "header element DOM parent null after detach");
    }

    // SAAJ Node 与 DOM Node 双视角：类型/节点名/节点类型/ownerDocument
    static void testSaaJDomDualView() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.Node n = body.addBodyElement(new javax.xml.namespace.QName("urn:n", "dual", "n"));
        check(n instanceof org.w3c.dom.Element, "SAAJ Node here is a DOM Element");
        check(n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE, "nodeType ELEMENT_NODE");
        check("dual".equals(n.getNodeName().substring(n.getNodeName().indexOf(':') + 1)),
                "DOM nodeName carries local name");
        check(n.getOwnerDocument() != null, "ownerDocument non-null");
        // RI 实测：元素 ownerDocument 是 SOAPPart 内部的 SOAPDocumentImpl（不直接 instanceof SOAPPart）
        check(n.getOwnerDocument() instanceof org.w3c.dom.Document,
                "ownerDocument is a DOM Document (RI internal SOAPDocumentImpl)");
        check(((javax.xml.soap.SOAPElement) n).getParentNode() instanceof javax.xml.soap.SOAPElement
                        || ((javax.xml.soap.SOAPElement) n).getParentNode() instanceof org.w3c.dom.Document,
                "parent is element or document in the SOAP tree");
        // 文本子节点双视角
        ((javax.xml.soap.SOAPElement) n).addTextNode("dv");
        java.util.Iterator<?> it = ((javax.xml.soap.SOAPElement) n).getChildElements();
        Object child = it.next();
        check(child instanceof javax.xml.soap.Node, "text child is SAAJ Node");
        check(child instanceof org.w3c.dom.Text, "text child is DOM Text");
        check(((javax.xml.soap.Node) child).getNodeType() == org.w3c.dom.Node.TEXT_NODE,
                "text child nodeType TEXT_NODE");
        check("dv".equals(((javax.xml.soap.Node) child).getValue()), "text child SAAJ value");
    }

    // setValue(null) 实测抛 NPE（RI 直接转 DOM setTextContent 语义）
    static void testNullNegative() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPElement el = body.addBodyElement(new javax.xml.namespace.QName("nullEl"));
        try {
            el.setValue(null);
            check(false, "setValue(null) must throw");
        } catch (java.lang.NullPointerException npe) {
            check(true, "setValue(null) throws NullPointerException (RI locked)");
        }
    }
}
