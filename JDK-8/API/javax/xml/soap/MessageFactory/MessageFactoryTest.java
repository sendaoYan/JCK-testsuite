/*
 * @test
 * @summary javax.xml.soap.MessageFactory conformance: protocol selection matrix (SOAP 1.1/1.2/Dynamic), message hierarchy chain, stream-based createMessage and its negative paths.
 * @run main MessageFactoryTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class MessageFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static int count(Iterator it) { int c = 0; while (it.hasNext()) { it.next(); c++; } return c; }

    // newInstance() 默认 SOAP 1.1：envelope 命名空间精确锁定
    static void testDefaultProtocolIsSoap11() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        check(mf != null, "factory non-null");
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        check(msg != null, "message non-null");
        check(msg.getSOAPPart() != null, "soap part non-null");
        javax.xml.soap.SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        check(env != null, "envelope non-null");
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(env.getNamespaceURI()),
                "default factory envelope ns is SOAP 1.1");
        check("Envelope".equals(env.getElementName().getLocalName()), "envelope local name");
    }

    // 显式协议矩阵：SOAP_1_1 / SOAP_1_2 / DEFAULT
    static void testExplicitProtocols() throws Exception {
        javax.xml.soap.MessageFactory mf11 = javax.xml.soap.MessageFactory.newInstance(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL);
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(mf11.createMessage().getSOAPPart().getEnvelope().getNamespaceURI()),
                "SOAP_1_1_PROTOCOL envelope ns");
        javax.xml.soap.MessageFactory mf12 = javax.xml.soap.MessageFactory.newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        javax.xml.soap.SOAPMessage m12 = mf12.createMessage();
        check("http://www.w3.org/2003/05/soap-envelope".equals(m12.getSOAPPart().getEnvelope().getNamespaceURI()),
                "SOAP_1_2_PROTOCOL envelope ns is 2003/05");
        javax.xml.soap.MessageFactory mfD = javax.xml.soap.MessageFactory.newInstance(javax.xml.soap.SOAPConstants.DEFAULT_SOAP_PROTOCOL);
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(mfD.createMessage().getSOAPPart().getEnvelope().getNamespaceURI()),
                "DEFAULT_SOAP_PROTOCOL == SOAP 1.1 envelope ns");
        // 1.1 与 1.2 默认消息均带 header/body
        check(m12.getSOAPHeader() != null, "1.2 default header present");
        check(m12.getSOAPBody() != null, "1.2 default body present");
        check(mf11.createMessage().getSOAPHeader() != null, "1.1 default header present");
    }

    // DYNAMIC_SOAP_PROTOCOL：工厂可创建，但 createMessage() 不支持（RI 实测 UnsupportedOperationException）
    static void testDynamicProtocol() throws Exception {
        javax.xml.soap.MessageFactory dyn = javax.xml.soap.MessageFactory.newInstance(javax.xml.soap.SOAPConstants.DYNAMIC_SOAP_PROTOCOL);
        check(dyn != null, "dynamic factory non-null");
        try {
            dyn.createMessage();
            check(false, "dynamic createMessage should throw");
        } catch (java.lang.UnsupportedOperationException expected) {
            check(true, "dynamic createMessage throws UnsupportedOperationException");
        }
    }

    // 未知协议串 → SOAPException 且消息包含该串
    static void testBogusProtocol() throws Exception {
        try {
            javax.xml.soap.MessageFactory.newInstance("bogus-proto");
            check(false, "newInstance(bogus) should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("bogus-proto"),
                    "SOAPException message contains 'bogus-proto': " + e.getMessage());
        }
        try {
            javax.xml.soap.MessageFactory.newInstance("");
            check(false, "newInstance(\"\") should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "empty protocol string rejected");
        }
    }

    // createMessage() 层级链：Part→Envelope→Header/Body 全非 null 且父子关系成立
    static void testHierarchyChain() throws Exception {
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPPart part = msg.getSOAPPart();
        javax.xml.soap.SOAPEnvelope env = part.getEnvelope();
        check(msg.getSOAPBody() != null, "body via message non-null");
        check(msg.getSOAPBody() == env.getBody(), "getSOAPBody() == envelope.getBody()");
        check(msg.getSOAPHeader() != null, "header via message non-null");
        check(msg.getSOAPHeader() == env.getHeader(), "getSOAPHeader() == envelope.getHeader()");
        check(env.getBody().getParentNode() == env, "body parent is envelope");
        check(env.getHeader().getParentNode() == env, "header parent is envelope");
        check(env.getParentElement() == null, "envelope has no SOAPElement parent");
        check(count(env.getChildElements()) == 2, "envelope has exactly header+body children");
        check(part == msg.getSOAPPart(), "getSOAPPart stable identity");
    }

    // 流式 createMessage：正常往返 + 空流 + 懒解析坏流
    static void testStreamCreateMessage() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage src = mf.createMessage();
        javax.xml.namespace.QName pay = new javax.xml.namespace.QName("http://x/", "pay", "p");
        src.getSOAPBody().addBodyElement(pay).addTextNode("99");
        src.saveChanges();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        src.writeTo(bo);
        javax.xml.soap.MimeHeaders hdrs = new javax.xml.soap.MimeHeaders();
        for (String h : src.getMimeHeaders().getHeader("Content-Type")) hdrs.addHeader("Content-Type", h);
        javax.xml.soap.SOAPMessage back = mf.createMessage(hdrs, new ByteArrayInputStream(bo.toByteArray()));
        check(back != null, "stream-created message non-null");
        check(back.getSOAPBody().getElementsByTagNameNS("http://x/", "pay").getLength() == 1,
                "stream round-trip keeps payload element");
        check(back.getSOAPBody().getTextContent().contains("99"), "stream round-trip keeps payload text");
        // null MimeHeaders 合法（空头）
        javax.xml.soap.SOAPMessage back2 = mf.createMessage(null, new ByteArrayInputStream(bo.toByteArray()));
        check(back2.getSOAPBody().getElementsByTagNameNS("http://x/", "pay").getLength() == 1,
                "createMessage(null headers, stream) works");
        // 空输入流：createMessage 本身返回对象（RI 懒解析），首次实现树时抛 SOAPException（实测锁定）
        javax.xml.soap.SOAPMessage empty = mf.createMessage(new javax.xml.soap.MimeHeaders(),
                new ByteArrayInputStream(new byte[0]));
        check(empty != null, "empty stream createMessage object still returned (lazy parse)");
        try {
            empty.getSOAPPart().getEnvelope();
            check(false, "empty stream envelope access should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("envelope"),
                    "empty stream SOAPException mentions envelope: " + e.getMessage());
        }
        // 坏 XML 流：构造成功但按需解析时报错（RI 懒解析，实测锁定）
        javax.xml.soap.SOAPMessage lazy = mf.createMessage(new javax.xml.soap.MimeHeaders(),
                new ByteArrayInputStream("<broken".getBytes("UTF-8")));
        check(lazy != null, "malformed stream createMessage object still returned (lazy parse)");
        try {
            lazy.getSOAPPart().getEnvelope();
            check(false, "malformed stream envelope access should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "malformed stream envelope access throws SOAPException lazily");
        }
        // 垃圾字节流同样懒失败
        javax.xml.soap.SOAPMessage garbage = mf.createMessage(new javax.xml.soap.MimeHeaders(),
                new ByteArrayInputStream("<<<>>>".getBytes("UTF-8")));
        check(garbage != null, "garbage stream createMessage object still returned");
        try {
            garbage.getSOAPBody();
            check(false, "garbage stream body access should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "garbage stream body access throws SOAPException lazily");
        }
    }

    public static void main(String[] args) throws Exception {
        testDefaultProtocolIsSoap11();
        testExplicitProtocols();
        testDynamicProtocol();
        testBogusProtocol();
        testHierarchyChain();
        testStreamCreateMessage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
