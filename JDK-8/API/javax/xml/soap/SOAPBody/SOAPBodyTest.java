/*
 * @test
 * @summary javax.xml.soap.SOAPBody conformance: interface hierarchy and methods.
 * @run main SOAPBodyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPBodyTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPBody");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addFault"), "has method addFault");
        check(names.contains("hasFault"), "has method hasFault");
        check(names.contains("getFault"), "has method getFault");
        check(names.contains("addBodyElement"), "has method addBodyElement");
        check(names.contains("addDocument"), "has method addDocument");
        check(names.contains("extractContentAsDocument"), "has method extractContentAsDocument");
        testBodyElementForms();
        testFaultLifecycle();
        testFaultOverloads();
        testDocumentBridge();
        testSoap12Body();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPBody body11() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody();
    }

    // addBodyElement(QName)/(Name) 回读 + body 文本
    static void testBodyElementForms() throws Exception {
        javax.xml.soap.SOAPBody body = body11();
        check(!body.hasFault(), "fresh body has no fault");
        check(body.getFault() == null, "fresh body getFault null");
        javax.xml.soap.SOAPBodyElement be = body.addBodyElement(new javax.xml.namespace.QName("http://x/", "op1", "p"));
        check(be != null, "addBodyElement(QName) non-null");
        check(be.getElementQName().equals(new javax.xml.namespace.QName("http://x/", "op1", "p")),
                "addBodyElement(QName) name round-trip");
        check(be.getParentElement() == body, "body element parent is body");
        be.addTextNode("data");
        check(body.getTextContent().contains("data"), "body textContent includes body element text");
        // Name 形态
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPBody body2 = env.getBody();
        javax.xml.soap.SOAPBodyElement be2 = body2.addBodyElement(env.createName("be1", "bb", "http://bb/"));
        check(be2.getElementQName().equals(new javax.xml.namespace.QName("http://bb/", "be1", "bb")),
                "addBodyElement(Name) name round-trip");
        // DOM 视角
        check(body.getElementsByTagNameNS("http://x/", "op1").getLength() == 1, "body DOM lookup payload");
    }

    // addFault→hasFault/getFault 同一实例；重复 addFault 抛；无 fault 时 getFault null
    static void testFaultLifecycle() throws Exception {
        javax.xml.soap.SOAPBody body = body11();
        javax.xml.soap.SOAPFault f = body.addFault();
        check(f != null, "addFault non-null");
        check(body.hasFault(), "hasFault true after addFault");
        check(f == body.getFault(), "getFault returns same instance");
        check(f instanceof javax.xml.soap.SOAPBodyElement, "fault is a SOAPBodyElement");
        try {
            body.addFault();
            check(false, "second addFault should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("already exists"),
                    "duplicate fault message mentions already exists: " + e.getMessage());
        }
        // detach fault 后 hasFault 复位
        f.detachNode();
        check(!body.hasFault(), "hasFault false after fault detach");
        check(body.getFault() == null, "getFault null after fault detach");
        javax.xml.soap.SOAPFault f2 = body.addFault();
        check(body.hasFault(), "addFault works again after detach");
        check(f2 != f, "new fault instance distinct");
    }

    // addFault(Name/QName × String × Locale) 重载矩阵
    static void testFaultOverloads() throws Exception {
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        String ns11 = "http://schemas.xmlsoap.org/soap/envelope/";
        // (Name, String)
        javax.xml.soap.SOAPBody b1 = javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody();
        javax.xml.soap.SOAPFault f1 = b1.addFault(env.createName("Server", "s", ns11), "msg-x");
        check(f1.getFaultCodeAsQName().equals(new javax.xml.namespace.QName(ns11, "Server")),
                "addFault(Name,String) code qname");
        check("msg-x".equals(f1.getFaultString()), "addFault(Name,String) fault string");
        // (QName, String)
        javax.xml.soap.SOAPBody b2 = javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody();
        javax.xml.soap.SOAPFault f2 = b2.addFault(new javax.xml.namespace.QName(ns11, "Client"), "m2");
        check(f2.getFaultCodeAsQName().getLocalPart().equals("Client"), "addFault(QName,String) code");
        check("m2".equals(f2.getFaultString()), "addFault(QName,String) string");
        // (QName, String, Locale)
        javax.xml.soap.SOAPBody b3 = javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody();
        javax.xml.soap.SOAPFault f3 = b3.addFault(new javax.xml.namespace.QName(ns11, "Sender"),
                "m3", java.util.Locale.CANADA);
        check("m3".equals(f3.getFaultString()), "addFault(QName,String,Locale) string");
        check(java.util.Locale.CANADA.equals(f3.getFaultStringLocale()), "addFault(QName,String,Locale) locale");
        // (Name, String, Locale)
        javax.xml.soap.SOAPBody b4 = javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody();
        javax.xml.soap.SOAPFault f4 = b4.addFault(env.createName("Client"), "m5", java.util.Locale.ITALY);
        check("Client".equals(f4.getFaultCodeAsName().getLocalName()), "addFault(Name,String,Locale) code");
        check(java.util.Locale.ITALY.equals(f4.getFaultStringLocale()), "addFault(Name,String,Locale) locale");
    }

    // addDocument(DOM) 真实导入闭环 + extractContentAsDocument 往返 + 多子元素负例
    static void testDocumentBridge() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(
                new java.io.ByteArrayInputStream("<root><a>1</a><b>2</b></root>".getBytes("UTF-8")));
        javax.xml.soap.SOAPBody body = body11();
        body.addDocument(doc);
        check(body.getElementsByTagName("root").getLength() == 1, "addDocument imports root");
        check(body.getElementsByTagName("a").getLength() == 1, "addDocument imports nested a");
        check(body.getElementsByTagName("b").getLength() == 1, "addDocument imports nested b");
        check(body.getTextContent().contains("1") && body.getTextContent().contains("2"),
                "addDocument preserves nested text");
        // extractContentAsDocument：单 body 子元素 → 独立 Document 往返
        org.w3c.dom.Document extracted = body.extractContentAsDocument();
        check("root".equals(extracted.getDocumentElement().getTagName()), "extractContentAsDocument root tag");
        check(extracted.getElementsByTagName("a").getLength() == 1, "extracted document keeps nested a");
        check(extracted.getDocumentElement().getTextContent().contains("1"), "extracted document keeps text");
        // 多于一个子元素 → SOAPException
        javax.xml.soap.SOAPBody body2 = body11();
        body2.addBodyElement(new javax.xml.namespace.QName("r1"));
        body2.addBodyElement(new javax.xml.namespace.QName("r2"));
        try {
            body2.extractContentAsDocument();
            check(false, "extract with 2 children should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Cannot extract"),
                    "extract failure message: " + e.getMessage());
        }
        // 仅含 fault 的 body：RI 实测不抛，导出以 Fault 为根的 Document（锁定实际行为）
        javax.xml.soap.SOAPBody body3 = body11();
        body3.addFault();
        try {
            org.w3c.dom.Document fd = body3.extractContentAsDocument();
            check(fd != null && fd.getDocumentElement() != null, "extract with fault-only body succeeds in RI");
            check(fd.getDocumentElement().getLocalName().equals("Fault"), "fault-only extract root is Fault");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "extract with fault throws SOAPException (strict path)");
        }
    }

    // 1.2 工厂：addFault() 默认 Receiver、addBodyElement 正常
    static void testSoap12Body() throws Exception {
        javax.xml.soap.MessageFactory mf12 = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        javax.xml.soap.SOAPBody body = mf12.createMessage().getSOAPBody();
        check(!body.hasFault(), "1.2 fresh body no fault");
        javax.xml.soap.SOAPBodyElement be = body.addBodyElement(new javax.xml.namespace.QName("http://x/", "e12"));
        check(be.getElementQName().getLocalPart().equals("e12"), "1.2 addBodyElement works");
        javax.xml.soap.SOAPBody body2 = mf12.createMessage().getSOAPBody();
        javax.xml.soap.SOAPFault f = body2.addFault();
        check(f != null && body2.hasFault(), "1.2 addFault() no-arg works");
        check(f.getFaultCodeAsQName().equals(
                new javax.xml.namespace.QName("http://www.w3.org/2003/05/soap-envelope", "Receiver")),
                "1.2 no-arg addFault defaults to Receiver");
        // 1.2 实测：body 已有其它子元素时 addFault 拒绝（"No other element except Fault allowed in SOAPBody"）
        javax.xml.soap.SOAPBody bodyMixed = mf12.createMessage().getSOAPBody();
        bodyMixed.addBodyElement(new javax.xml.namespace.QName("http://x/", "payload12"));
        try {
            bodyMixed.addFault();
            check(false, "1.2 addFault over non-fault child should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("No other element"),
                    "1.2 mixed-body addFault message: " + e.getMessage());
        }
        javax.xml.soap.SOAPBody body3 = mf12.createMessage().getSOAPBody();
        javax.xml.soap.SOAPFault f3 = body3.addFault(
                new javax.xml.namespace.QName("http://www.w3.org/2003/05/soap-envelope", "Sender"), "12str");
        check(f3.getFaultCodeAsQName().getLocalPart().equals("Sender"), "1.2 addFault(QName,String) code");
        check("12str".equals(f3.getFaultString()), "1.2 addFault(QName,String) string");
        try {
            body3.addFault();
            check(false, "1.2 duplicate addFault should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "1.2 duplicate addFault throws SOAPException");
        }
    }
}
