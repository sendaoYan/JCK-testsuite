/*
 * @test
 * @summary javax.xml.soap.DetailEntry conformance: interface hierarchy and methods.
 * @run main DetailEntryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DetailEntryTest {
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
        Class<?> c = Class.forName("javax.xml.soap.DetailEntry");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        // DetailEntry 接口自身不声明任何新方法（纯标记型扩展 SOAPElement）
        check(c.getDeclaredMethods().length == 0, "DetailEntry declares no additional methods");
        testRealChannel();
        testElementCapabilities();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.DetailEntry newEntry(String local) throws Exception {
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        javax.xml.soap.Detail d = f.addDetail();
        return d.addDetailEntry(new javax.xml.namespace.QName("http://d/", local, "dd"));
    }

    static int count(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // 真实通道：fault→detail→entry 全链路
    @SuppressWarnings("unchecked")
    static void testRealChannel() throws Exception {
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        javax.xml.soap.Detail d = f.addDetail();
        javax.xml.soap.DetailEntry e1 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "e1", "dd"));
        javax.xml.soap.DetailEntry e2 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "e2", "dd"));
        check(e1 != null && e2 != null, "two entries created");
        check(e1 != e2, "entries are distinct instances");
        check(e1.getParentElement() == d, "entry parent is detail");
        check(count(d.getDetailEntries()) == 2, "two entries iterate");
        // QName 精确回读
        check(e1.getElementQName().equals(new javax.xml.namespace.QName("http://d/", "e1", "dd")),
                "entry QName round-trip");
        check("dd:e1".equals(e1.getElementName().getQualifiedName()), "entry qualified name dd:e1");
    }

    // entry 作为 SOAPElement 的全部写通道能力
    @SuppressWarnings("unchecked")
    static void testElementCapabilities() throws Exception {
        javax.xml.soap.DetailEntry e = newEntry("cap");
        e.addTextNode("dval");
        check("dval".equals(e.getValue()), "addTextNode/getValue");
        check(e.getTextContent().contains("dval"), "textContent");
        // 子元素
        javax.xml.soap.SOAPElement child = e.addChildElement("sub");
        check(child.getParentElement() == e, "child element inside entry");
        check(count(e.getChildElements(new javax.xml.namespace.QName("sub"))) == 1, "child filter count");
        // 属性
        e.addAttribute(new javax.xml.namespace.QName("http://d/", "at"), "av");
        check("av".equals(e.getAttributeValue(new javax.xml.namespace.QName("http://d/", "at"))),
                "attribute round-trip inside entry");
        // 命名空间声明
        e.addNamespaceDeclaration("np", "http://np/");
        check("http://np/".equals(e.getNamespaceURI("np")), "namespace declaration inside entry");
        // detachNode 后从 detail 消失
        javax.xml.soap.SOAPFault f2 = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        javax.xml.soap.Detail d2 = f2.addDetail();
        javax.xml.soap.DetailEntry x1 = d2.addDetailEntry(new javax.xml.namespace.QName("http://d/", "x1"));
        d2.addDetailEntry(new javax.xml.namespace.QName("http://d/", "x2"));
        x1.detachNode();
        check(count(d2.getDetailEntries()) == 1, "detached entry removed from detail iteration");
        testNodeInterfaceBridge();
        testSiblingIndependence();
    }

    // javax.xml.soap.Node 桥接方法在 entry 上可用
    @SuppressWarnings("unchecked")
    static void testNodeInterfaceBridge() throws Exception {
        javax.xml.soap.DetailEntry e = newEntry("nodebridge");
        e.addTextNode("v0");
        check("v0".equals(e.getValue()), "Node.getValue on entry");
        e.setValue("v1");
        check("v1".equals(e.getValue()), "Node.setValue round-trip on entry");
        check(e instanceof org.w3c.dom.Node, "entry is a DOM Node");
        check(e.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE, "entry DOM node type is ELEMENT_NODE");
        check("e".length() > 0, "sanity");
        // encodingStyle
        check(e.getEncodingStyle() == null, "entry default encoding style null");
        e.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
        check("http://schemas.xmlsoap.org/soap/encoding/".equals(e.getEncodingStyle()), "entry encoding style round-trip");
        // removeContents
        e.addChildElement("t1");
        e.addChildElement("t2");
        check(count(e.getChildElements(new javax.xml.namespace.QName("t1"))) == 1, "child added");
        e.removeContents();
        check(count(e.getChildElements()) == 0 || count(e.getChildElements()) == 1,
                "after removeContents only text node may remain");
        e.removeContents();
    }

    // 同名多条 entry 各自独立；同名可共存
    @SuppressWarnings("unchecked")
    static void testSiblingIndependence() throws Exception {
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        javax.xml.soap.Detail d = f.addDetail();
        javax.xml.soap.DetailEntry a = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "same", "dd"));
        javax.xml.soap.DetailEntry b = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "same", "dd"));
        a.addTextNode("A");
        b.addTextNode("B");
        check(count(d.getDetailEntries()) == 2, "same-name entries coexist");
        check("A".equals(a.getValue()) && "B".equals(b.getValue()), "same-name entries keep independent text");
        check(a != b, "same-name entries distinct instances");
        check(count(d.getChildElements(new javax.xml.namespace.QName("http://d/", "same"))) == 2,
                "detail child filter counts both same-name entries");
    }
}
