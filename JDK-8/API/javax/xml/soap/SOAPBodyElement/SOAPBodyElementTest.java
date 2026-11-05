/*
 * @test
 * @summary javax.xml.soap.SOAPBodyElement conformance: 真实消息通道回读(QName/Name/父链/DOM 查找)/文本与属性往返/编码风格/嵌套子元素/类型层次分叉(SOAPFault 是 BodyElement 非 FaultElement).
 * @run main SOAPBodyElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPBodyElementTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPBodyElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        check(c.getDeclaredMethods().length == 0, "marker interface: no own methods (typing only)");
        testRealChannel();
        testTextAndAttributes();
        testEncodingStyle();
        testNestedChildren();
        testNameForm();
        testFaultTyping();
        testMultipleAndDetach();
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

    // 真实通道：body.addBodyElement(QName) 名称/类型/父链/DOM 可见性全回读
    static void testRealChannel() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPBodyElement be = body.addBodyElement(
                new javax.xml.namespace.QName("urn:op", "op1", "p"));
        check(be != null, "addBodyElement(QName) non-null");
        check(be instanceof javax.xml.soap.SOAPBodyElement, "product is SOAPBodyElement");
        check(be instanceof javax.xml.soap.SOAPElement, "product is SOAPElement");
        check(be.getElementQName().equals(new javax.xml.namespace.QName("urn:op", "op1", "p")),
                "getElementQName exact round-trip");
        check("op1".equals(be.getElementName().getLocalName()), "getElementName local");
        check("p".equals(be.getElementName().getPrefix()), "getElementName prefix");
        check("urn:op".equals(be.getElementName().getURI()), "getElementName uri");
        check("op1".equals(be.getLocalName()), "DOM localName");
        check("urn:op".equals(be.getNamespaceURI()), "DOM namespace");
        check("p".equals(be.getPrefix()), "DOM prefix");
        check(be.getParentElement() == body, "SAAJ parent is body");
        check(be.getParentNode() == body, "DOM parent is body");
        check(body.getElementsByTagNameNS("urn:op", "op1").getLength() == 1, "DOM lookup finds it");
        check(count(body.getChildElements()) == 1, "body child count 1");
        check(count(body.getChildElements(new javax.xml.namespace.QName("urn:op", "op1"))) == 1,
                "filtered getChildElements(QName) count 1");
    }

    // addTextNode / addAttribute / getAttributeValue / removeAttribute 往返
    static void testTextAndAttributes() throws Exception {
        javax.xml.soap.SOAPBodyElement be = body().addBodyElement(
                new javax.xml.namespace.QName("urn:op", "op2", "p"));
        be.addTextNode("data");
        check("data".equals(be.getValue()), "getValue after addTextNode");
        check("data".equals(be.getTextContent()), "getTextContent after addTextNode");
        javax.xml.namespace.QName attr = new javax.xml.namespace.QName("urn:attr", "id", "a");
        be.addAttribute(attr, "v1");
        check("v1".equals(be.getAttributeValue(attr)), "getAttributeValue round-trip");
        be.addAttribute(attr, "v2");
        check("v2".equals(be.getAttributeValue(attr)), "re-addAttribute overwrites");
        check(be.getAttributeValue(new javax.xml.namespace.QName("urn:attr", "nope")) == null,
                "absent attribute returns null");
        check(be.removeAttribute(attr), "removeAttribute returns true");
        check(be.getAttributeValue(attr) == null, "attribute gone after remove");
        check(!be.removeAttribute(attr), "second remove returns false");
        be.addNamespaceDeclaration("px", "urn:x");
        check("urn:x".equals(be.getNamespaceURI("px")), "namespace declaration round-trip");
    }

    // setEncodingStyle/getEncodingStyle 回读
    static void testEncodingStyle() throws Exception {
        javax.xml.soap.SOAPBodyElement be = body().addBodyElement(
                new javax.xml.namespace.QName("urn:op", "op3", "p"));
        check(be.getEncodingStyle() == null, "fresh element has no encoding style");
        String enc = javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENCODING;
        be.setEncodingStyle(enc);
        check(enc.equals(be.getEncodingStyle()), "encoding style round-trip");
        String enc2 = "urn:custom-enc";
        be.setEncodingStyle(enc2);
        check(enc2.equals(be.getEncodingStyle()), "encoding style overwrite");
    }

    // addChildElement 嵌套：计数/父链/setValue
    static void testNestedChildren() throws Exception {
        javax.xml.soap.SOAPBodyElement be = body().addBodyElement(
                new javax.xml.namespace.QName("urn:op", "op4", "p"));
        javax.xml.soap.SOAPElement kid1 = be.addChildElement(new javax.xml.namespace.QName("urn:op", "k1", "p"));
        javax.xml.soap.SOAPElement kid2 = be.addChildElement("k2");
        check(kid1.getParentElement() == be, "nested kid1 parent is body element");
        check(kid2.getParentElement() == be, "nested kid2 parent is body element");
        check(count(be.getChildElements()) == 2, "nested child count 2");
        check(count(be.getChildElements(new javax.xml.namespace.QName("urn:op", "k1"))) == 1,
                "filtered nested count 1");
        kid1.setValue("nested-value");
        check("nested-value".equals(kid1.getValue()), "nested kid setValue/getValue");
        check(be.getTextContent().contains("nested-value"), "parent textContent includes nested text");
        // removeContents 清空嵌套
        be.removeContents();
        check(count(be.getChildElements()) == 0, "removeContents clears nested children");
        check(be.getTextContent().isEmpty(), "textContent empty after removeContents");
    }

    // Name 形态（envelope.createName）
    static void testNameForm() throws Exception {
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPBodyElement be = env.getBody().addBodyElement(env.createName("be1", "bb", "urn:bb"));
        check(be.getElementQName().equals(new javax.xml.namespace.QName("urn:bb", "be1", "bb")),
                "addBodyElement(Name) QName round-trip");
        check("urn:bb".equals(be.getNamespaceURI()), "addBodyElement(Name) DOM namespace");
        // setElementQName 改名
        javax.xml.soap.SOAPElement renamed = be.setElementQName(new javax.xml.namespace.QName("urn:bb", "be1x", "bb"));
        check(renamed.getElementQName().getLocalPart().equals("be1x"), "setElementQName renames");
        check(renamed.getParentElement() == env.getBody(), "renamed element stays child of body");
    }

    // 类型层次分叉：SOAPFault instanceof SOAPBodyElement 但不是 SOAPFaultElement
    static void testFaultTyping() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPFault fault = body.addFault();
        check(fault instanceof javax.xml.soap.SOAPBodyElement,
                "SOAPFault IS a SOAPBodyElement (per SAAJ interface hierarchy)");
        check(!(fault instanceof javax.xml.soap.SOAPFaultElement),
                "SOAPFault is NOT a SOAPFaultElement (fault children are)");
        check(body.hasFault(), "body with fault reports hasFault");
    }

    // 多 body 元素共存 + detach 生命周期
    static void testMultipleAndDetach() throws Exception {
        javax.xml.soap.SOAPBody body = body();
        javax.xml.soap.SOAPBodyElement e1 = body.addBodyElement(new javax.xml.namespace.QName("m1"));
        javax.xml.soap.SOAPBodyElement e2 = body.addBodyElement(new javax.xml.namespace.QName("m2"));
        check(e1 != e2, "two body elements distinct");
        check(count(body.getChildElements()) == 2, "two body elements counted");
        e1.detachNode();
        check(count(body.getChildElements()) == 1, "count 1 after detach first");
        check(e1.getParentElement() == null, "detached element parent null");
        e2.detachNode();
        check(count(body.getChildElements()) == 0, "count 0 after detach second");
        check(body.getElementsByTagName("m2").getLength() == 0, "DOM lookup empty after detach");
    }
}
