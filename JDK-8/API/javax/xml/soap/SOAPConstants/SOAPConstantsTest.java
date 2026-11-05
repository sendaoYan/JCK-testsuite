/*
 * @test
 * @summary javax.xml.soap.SOAPConstants conformance: interface hierarchy and methods.
 * @run main SOAPConstantsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPConstantsTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPConstants");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        testProtocolSelectors();
        testNamespaceUris();
        testContentTypes();
        testActorRoleUris();
        testFaultQNames();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 协议选择器常量（4 条）
    static void testProtocolSelectors() throws Exception {
        check("Dynamic Protocol".equals(javax.xml.soap.SOAPConstants.DYNAMIC_SOAP_PROTOCOL), "DYNAMIC_SOAP_PROTOCOL value");
        check("SOAP 1.1 Protocol".equals(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL), "SOAP_1_1_PROTOCOL value");
        check("SOAP 1.2 Protocol".equals(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL), "SOAP_1_2_PROTOCOL value");
        check("SOAP 1.1 Protocol".equals(javax.xml.soap.SOAPConstants.DEFAULT_SOAP_PROTOCOL),
                "DEFAULT_SOAP_PROTOCOL == SOAP 1.1 Protocol");
        check(javax.xml.soap.SOAPConstants.DEFAULT_SOAP_PROTOCOL.equals(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL),
                "DEFAULT == SOAP_1_1 selector");
        // 选择器直接驱动工厂（常量真实可用性闭环）
        javax.xml.soap.SOAPEnvelope e11 = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL)
                .createMessage().getSOAPPart().getEnvelope();
        check(e11.getNamespaceURI().equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE),
                "SOAP_1_1_PROTOCOL produces 1.1 envelope ns");
        javax.xml.soap.SOAPEnvelope e12 = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL)
                .createMessage().getSOAPPart().getEnvelope();
        check(e12.getNamespaceURI().equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE),
                "SOAP_1_2_PROTOCOL produces 1.2 envelope ns");
    }

    // 命名空间 URI 常量（5 条）
    static void testNamespaceUris() throws Exception {
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE),
                "URI_NS_SOAP_1_1_ENVELOPE");
        check("http://www.w3.org/2003/05/soap-envelope".equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE),
                "URI_NS_SOAP_1_2_ENVELOPE");
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE),
                "URI_NS_SOAP_ENVELOPE == 1.1 envelope");
        check("http://schemas.xmlsoap.org/soap/encoding/".equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENCODING),
                "URI_NS_SOAP_ENCODING");
        check("http://www.w3.org/2003/05/soap-encoding".equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENCODING),
                "URI_NS_SOAP_1_2_ENCODING");
    }

    // 内容类型与 SOAP_ENV_PREFIX（3 条）
    static void testContentTypes() throws Exception {
        check("text/xml".equals(javax.xml.soap.SOAPConstants.SOAP_1_1_CONTENT_TYPE), "SOAP_1_1_CONTENT_TYPE");
        check("application/soap+xml".equals(javax.xml.soap.SOAPConstants.SOAP_1_2_CONTENT_TYPE), "SOAP_1_2_CONTENT_TYPE");
        check("env".equals(javax.xml.soap.SOAPConstants.SOAP_ENV_PREFIX), "SOAP_ENV_PREFIX");
        // 实际消息 Content-Type 以 SOAP_1_1_CONTENT_TYPE 开头（行为闭环）
        javax.xml.soap.SOAPMessage m = javax.xml.soap.MessageFactory.newInstance().createMessage();
        m.saveChanges();
        String[] ct = m.getMimeHeaders().getHeader("Content-Type");
        check(ct != null && ct[0].startsWith(javax.xml.soap.SOAPConstants.SOAP_1_1_CONTENT_TYPE),
                "saved 1.1 message Content-Type starts with constant: " + (ct == null ? "null" : ct[0]));
    }

    // actor/role URI 常量（4 条）
    static void testActorRoleUris() throws Exception {
        check("http://schemas.xmlsoap.org/soap/actor/next".equals(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT),
                "URI_SOAP_ACTOR_NEXT");
        check("http://www.w3.org/2003/05/soap-envelope/role/next".equals(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT),
                "URI_SOAP_1_2_ROLE_NEXT");
        check("http://www.w3.org/2003/05/soap-envelope/role/none".equals(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NONE),
                "URI_SOAP_1_2_ROLE_NONE");
        check("http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver".equals(
                javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER),
                "URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER");
        // NEXT actor 常量直接作为 header 过滤器（行为闭环）
        javax.xml.soap.SOAPHeader h = javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "n"));
        he.setActor(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT);
        check(h.examineHeaderElements(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT).hasNext(),
                "URI_SOAP_ACTOR_NEXT usable as filter");
    }

    // 5 个标准 fault QName（ns == 1.2 envelope，前缀 env）
    static void testFaultQNames() throws Exception {
        String ns12 = "http://www.w3.org/2003/05/soap-envelope";
        check(javax.xml.soap.SOAPConstants.SOAP_VERSIONMISMATCH_FAULT.equals(
                new javax.xml.namespace.QName(ns12, "VersionMismatch", "env")),
                "SOAP_VERSIONMISMATCH_FAULT qname");
        check(javax.xml.soap.SOAPConstants.SOAP_MUSTUNDERSTAND_FAULT.equals(
                new javax.xml.namespace.QName(ns12, "MustUnderstand", "env")),
                "SOAP_MUSTUNDERSTAND_FAULT qname");
        check(javax.xml.soap.SOAPConstants.SOAP_DATAENCODINGUNKNOWN_FAULT.equals(
                new javax.xml.namespace.QName(ns12, "DataEncodingUnknown", "env")),
                "SOAP_DATAENCODINGUNKNOWN_FAULT qname");
        check(javax.xml.soap.SOAPConstants.SOAP_SENDER_FAULT.equals(
                new javax.xml.namespace.QName(ns12, "Sender", "env")),
                "SOAP_SENDER_FAULT qname");
        check(javax.xml.soap.SOAPConstants.SOAP_RECEIVER_FAULT.equals(
                new javax.xml.namespace.QName(ns12, "Receiver", "env")),
                "SOAP_RECEIVER_FAULT qname");
        // SENDER fault 常量直接设置 1.2 fault code（行为闭环）
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL)
                .createMessage().getSOAPBody().addFault();
        f.setFaultCode(javax.xml.soap.SOAPConstants.SOAP_SENDER_FAULT);
        check(f.getFaultCodeAsQName().equals(javax.xml.soap.SOAPConstants.SOAP_SENDER_FAULT),
                "SOAP_SENDER_FAULT directly usable as 1.2 fault code");
    }
}
