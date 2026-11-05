/*
 * @test
 * @summary javax.xml.soap.SOAPPart conformance: abstract class hierarchy and methods.
 * @run main SOAPPartTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPPartTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPPart");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.w3c.dom.Document"), "implements org.w3c.dom.Document");
        check(has(ifs, "javax.xml.soap.Node"), "implements javax.xml.soap.Node");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEnvelope"), "has method getEnvelope");
        check(names.contains("getContentId"), "has method getContentId");
        check(names.contains("getContentLocation"), "has method getContentLocation");
        check(names.contains("setContentId"), "has method setContentId");
        check(names.contains("setContentLocation"), "has method setContentLocation");
        check(names.contains("removeMimeHeader"), "has method removeMimeHeader");
        check(names.contains("removeAllMimeHeaders"), "has method removeAllMimeHeaders");
        check(names.contains("getMimeHeader"), "has method getMimeHeader");
        check(names.contains("setMimeHeader"), "has method setMimeHeader");
        check(names.contains("addMimeHeader"), "has method addMimeHeader");
        check(names.contains("getAllMimeHeaders"), "has method getAllMimeHeaders");
        check(names.contains("getMatchingMimeHeaders"), "has method getMatchingMimeHeaders");
        check(names.contains("getNonMatchingMimeHeaders"), "has method getNonMatchingMimeHeaders");
        check(names.contains("setContent"), "has method setContent");
        check(names.contains("getContent"), "has method getContent");
        testEnvelopeAndContent();
        testMimeHeaderFamily();
        testDomView();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.MessageFactory mf() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance();
    }

    // getEnvelope 非 null + getContent() 为 Source（RI 为 DOMSource）+ setContent(StreamSource) 回读
    static void testEnvelopeAndContent() throws Exception {
        javax.xml.soap.SOAPMessage msg = mf().createMessage();
        javax.xml.soap.SOAPPart part = msg.getSOAPPart();
        javax.xml.soap.SOAPEnvelope env = part.getEnvelope();
        check(env != null, "getEnvelope non-null");
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(env.getNamespaceURI()), "envelope ns SOAP 1.1");
        check(part.getEnvelope() == env, "getEnvelope stable identity");
        // getContent() instanceof javax.xml.transform.Source（RI 实测为 DOMSource）
        javax.xml.transform.Source content = part.getContent();
        check(content != null, "getContent non-null");
        check(content instanceof javax.xml.transform.Source, "getContent is a Source");
        check("javax.xml.transform.dom.DOMSource".equals(content.getClass().getName()),
                "RI getContent returns DOMSource: " + content.getClass().getName());
        // setContent(StreamSource) 换入新文档后 envelope 命名空间正确
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soapenv:Body><p:pay xmlns:p=\"http://x/\">7</p:pay></soapenv:Body></soapenv:Envelope>";
        part.setContent(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xml)));
        javax.xml.soap.SOAPEnvelope env2 = part.getEnvelope();
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(env2.getNamespaceURI()),
                "after setContent envelope ns preserved");
        check(env2.getBody().getElementsByTagNameNS("http://x/", "pay").getLength() == 1,
                "after setContent imported body element found");
        check(env2.getBody().getTextContent().contains("7"), "after setContent imported text found");
        // SOAPPart 同时实现 org.w3c.dom.Document
        check(part instanceof org.w3c.dom.Document, "SOAPPart is a DOM Document");
        check(part.getFirstChild() != null, "SOAPPart has first child (envelope)");
        check("Envelope".equals(part.getDocumentElement().getLocalName()), "document element is Envelope");
    }

    // addMimeHeader/getMimeHeader/setMimeHeader/remove*/getAll*/getMatching* 全家族
    @SuppressWarnings("unchecked")
    static void testMimeHeaderFamily() throws Exception {
        javax.xml.soap.SOAPPart part = mf().createMessage().getSOAPPart();
        part.removeAllMimeHeaders();
        check(countIter(part.getAllMimeHeaders()) == 0, "after removeAllMimeHeaders empty");
        part.addMimeHeader("X-Part", "pv1");
        part.addMimeHeader("X-Part", "pv2");
        String[] multi = part.getMimeHeader("X-Part");
        check(multi != null && multi.length == 2, "addMimeHeader keeps both values");
        check("pv1".equals(multi[0]) && "pv2".equals(multi[1]), "addMimeHeader preserves insertion order");
        // 大小写不敏感查找
        check(part.getMimeHeader("x-part") != null && part.getMimeHeader("x-part").length == 2,
                "getMimeHeader lookup case-insensitive");
        check(part.getMimeHeader("X-PART") != null, "getMimeHeader uppercase lookup hits");
        check(part.getMimeHeader("X-Missing") == null, "getMimeHeader unknown returns null");
        // setMimeHeader 替换全部旧值
        part.setMimeHeader("X-Part", "setv");
        String[] after = part.getMimeHeader("X-Part");
        check(after.length == 1 && "setv".equals(after[0]), "setMimeHeader replaces all values");
        part.addMimeHeader("X-Other", "ov");
        // getAllMimeHeaders 迭代元素均为 MimeHeader 且 name/value 回读
        java.util.Iterator it = part.getAllMimeHeaders();
        int n = 0;
        boolean sawPart = false, sawOther = false;
        while (it.hasNext()) {
            Object o = it.next();
            check(o instanceof javax.xml.soap.MimeHeader, "iterator element is MimeHeader");
            javax.xml.soap.MimeHeader h = (javax.xml.soap.MimeHeader) o;
            n++;
            if ("X-Part".equals(h.getName()) && "setv".equals(h.getValue())) sawPart = true;
            if ("X-Other".equals(h.getName()) && "ov".equals(h.getValue())) sawOther = true;
        }
        check(n == 2, "getAllMimeHeaders yields 2 headers");
        check(sawPart && sawOther, "all header name/value pairs readable");
        // getMatchingHeaders / getNonMatchingMimeHeaders
        check(countIter(part.getMatchingMimeHeaders(new String[]{"X-Part"})) == 1, "matching 1 for X-Part");
        check(countIter(part.getNonMatchingMimeHeaders(new String[]{"X-Part"})) == 1, "non-matching 1 for X-Part");
        check(countIter(part.getMatchingMimeHeaders(new String[0])) == 0, "matching 0 for empty pattern");
        check(countIter(part.getNonMatchingMimeHeaders(new String[0])) == 2, "non-matching 2 for empty pattern");
        // removeMimeHeader 单删
        part.removeMimeHeader("X-Other");
        check(part.getMimeHeader("X-Other") == null, "removeMimeHeader removes");
        check(countIter(part.getAllMimeHeaders()) == 1, "1 header left after removeMimeHeader");
        part.removeMimeHeader("X-Never-Existed");
        check(countIter(part.getAllMimeHeaders()) == 1, "removeMimeHeader of unknown is no-op");
        // setContentId / getContentId（映射 Content-Id 头）
        part.setContentId("cid-1");
        check("cid-1".equals(part.getContentId()), "contentId round-trip");
        check(part.getMimeHeader("Content-Id") != null && part.getMimeHeader("Content-Id")[0].equals("cid-1"),
                "setContentId backs Content-Id header");
        check(part.getMimeHeader("content-id") != null, "Content-Id lookup case-insensitive");
        // setContentLocation / getContentLocation
        part.setContentLocation("loc-1");
        check("loc-1".equals(part.getContentLocation()), "contentLocation round-trip");
        check(part.getMimeHeader("Content-Location") != null, "setContentLocation backs Content-Location header");
        // removeAllMimeHeaders 清空含 Content-Id
        part.removeAllMimeHeaders();
        check(part.getContentId() == null, "contentId null after removeAllMimeHeaders");
        check(part.getContentLocation() == null, "contentLocation null after removeAllMimeHeaders");
        check(countIter(part.getAllMimeHeaders()) == 0, "all mime headers cleared");
    }

    // DOM 视角：getElementsByTagNameNS / getElementsByTagName / 子节点计数
    static void testDomView() throws Exception {
        javax.xml.soap.SOAPMessage msg = mf().createMessage();
        javax.xml.soap.SOAPPart part = msg.getSOAPPart();
        javax.xml.namespace.QName pay = new javax.xml.namespace.QName("http://x/", "pay", "p");
        msg.getSOAPBody().addBodyElement(pay).addTextNode("99");
        // 先实现树（getEnvelope/getSOAPBody）再 DOM 查询（RI 实测稳定路径）
        part.getEnvelope();
        msg.getSOAPBody();
        check(part.getElementsByTagNameNS("http://x/", "pay").getLength() == 1,
                "DOM getElementsByTagNameNS finds payload");
        check(part.getElementsByTagNameNS("*", "pay").getLength() == 1, "DOM wildcard ns lookup");
        check(part.getElementsByTagNameNS("http://x/", "nopay").getLength() == 0, "DOM unknown lookup 0");
        check(part.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body").getLength() == 1,
                "DOM finds soap Body");
        check(part.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Header").getLength() == 1,
                "DOM finds soap Header");
        // DOM getElementsByTagName 匹配的是限定标签名（前缀:local），非 local 名（DOM 契约，实测锁定）
        check(part.getElementsByTagName("p:pay").getLength() == 1, "DOM getElementsByTagName matches qualified tag p:pay");
        check(part.getElementsByTagName("pay").getLength() == 0, "DOM getElementsByTagName does not match bare local name");
        check(part.getElementsByTagName("SOAP-ENV:Body").getLength() == 1, "DOM getElementsByTagName finds SOAP-ENV:Body");
        check(part.getChildNodes().getLength() == 1, "document has single envelope child");
        check(part.getDocumentElement() == part.getEnvelope(), "documentElement is envelope");
    }

    static int countIter(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }
}
