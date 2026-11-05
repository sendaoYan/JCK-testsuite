/*
 * @test
 * @summary javax.xml.soap.SOAPEnvelope conformance: interface hierarchy and methods.
 * @run main SOAPEnvelopeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPEnvelopeTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPEnvelope");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createName"), "has method createName");
        check(names.contains("getHeader"), "has method getHeader");
        check(names.contains("getBody"), "has method getBody");
        check(names.contains("addHeader"), "has method addHeader");
        check(names.contains("addBody"), "has method addBody");
        testStructureAndParentage();
        testAddHeaderBodySemantics();
        testCreateNameForms();
        testProtocolNamespaceMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // getHeader()/getBody() 非 null；header/body 父节点均为 envelope；envelope 自身元数据
    @SuppressWarnings("unchecked")
    static void testStructureAndParentage() throws Exception {
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        check(env.getHeader() != null, "getHeader non-null on default message");
        check(env.getBody() != null, "getBody non-null on default message");
        check(env.getHeader().getParentNode() == env, "header parentNode is envelope");
        check(env.getBody().getParentNode() == env, "body parentNode is envelope");
        check(env.getHeader().getParentElement() == env, "header parentElement is envelope");
        check(env.getBody().getParentElement() == env, "body parentElement is envelope");
        check(env instanceof javax.xml.soap.SOAPElement, "envelope is a SOAPElement");
        check("Envelope".equals(env.getElementName().getLocalName()), "envelope element local name");
        check("SOAP-ENV".equals(env.getElementName().getPrefix()), "envelope element prefix SOAP-ENV");
        check(env.getElementQName().equals(
                new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope")),
                "envelope element QName");
        // header/body 类型面
        check(env.getHeader() instanceof javax.xml.soap.SOAPHeader, "header implements SOAPHeader");
        check(env.getBody() instanceof javax.xml.soap.SOAPBody, "body implements SOAPBody");
        // detach 后 getHeader 返回 null
        env.getHeader().detachNode();
        check(env.getHeader() == null, "getHeader null after detachNode");
        env.getBody().detachNode();
        check(env.getBody() == null, "getBody null after detachNode");
    }

    // addHeader/addBody：默认消息已带 header/body → 再加抛 SOAPException；detach 后可重建
    static void testAddHeaderBodySemantics() throws Exception {
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        try {
            env.addHeader();
            check(false, "addHeader with existing header should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("header"),
                    "addHeader duplicate message mentions header: " + e.getMessage());
        }
        javax.xml.soap.SOAPHeader existing = env.getHeader();
        check(existing != null, "header present before detach");
        existing.detachNode();
        javax.xml.soap.SOAPHeader readded = env.addHeader();
        check(readded != null, "addHeader succeeds after detach");
        check(readded.getParentNode() == env, "re-added header parent is envelope");
        try {
            env.addHeader();
            check(false, "addHeader twice should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "addHeader twice throws SOAPException");
        }
        try {
            env.addBody();
            check(false, "addBody with existing body should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("body"),
                    "addBody duplicate message mentions body: " + e.getMessage());
        }
        env.getBody().detachNode();
        javax.xml.soap.SOAPBody readdedBody = env.addBody();
        check(readdedBody != null, "addBody succeeds after detach");
        check(readdedBody.getParentNode() == env, "re-added body parent is envelope");
        try {
            env.addBody();
            check(false, "addBody twice should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "addBody twice throws SOAPException");
        }
    }

    // createName(local) / createName(local,prefix,uri) 回读 + 等值语义
    static void testCreateNameForms() throws Exception {
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.Name n1 = env.createName("ln");
        check("ln".equals(n1.getLocalName()), "createName(local) local part");
        check("".equals(n1.getPrefix()), "createName(local) prefix empty");
        check("".equals(n1.getURI()), "createName(local) uri empty");
        check("ln".equals(n1.getQualifiedName()), "createName(local) qualified name");
        javax.xml.soap.Name n2 = env.createName("ln2", "px", "http://u/");
        check("ln2".equals(n2.getLocalName()), "createName 3-arg local part");
        check("px".equals(n2.getPrefix()), "createName 3-arg prefix");
        check("http://u/".equals(n2.getURI()), "createName 3-arg uri");
        check("px:ln2".equals(n2.getQualifiedName()), "createName 3-arg qualified name 'prefix:local'");
        // 同参两实例 equals/hashCode 一致
        javax.xml.soap.Name n1b = env.createName("ln");
        check(n1.equals(n1b), "same-param Names equal");
        check(n1.hashCode() == n1b.hashCode(), "same-param Names hashCode equal");
        check(!n1.equals(n2), "different Names not equal");
        check(!n1.equals(null), "Name not equal to null");
    }

    // 1.1 vs 1.2 工厂：envelope 命名空间全等矩阵 + 结构等价
    static void testProtocolNamespaceMatrix() throws Exception {
        javax.xml.soap.SOAPEnvelope env11 = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPEnvelope env11e = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL)
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPEnvelope env12 = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL)
                .createMessage().getSOAPPart().getEnvelope();
        String ns11 = "http://schemas.xmlsoap.org/soap/envelope/";
        String ns12 = "http://www.w3.org/2003/05/soap-envelope";
        check(ns11.equals(env11.getNamespaceURI()), "default envelope ns == 1.1");
        check(ns11.equals(env11e.getNamespaceURI()), "explicit 1.1 factory envelope ns == 1.1");
        check(ns12.equals(env12.getNamespaceURI()), "1.2 factory envelope ns == 1.2");
        check(!env11.getNamespaceURI().equals(env12.getNamespaceURI()), "1.1 and 1.2 ns differ");
        check(env11.getNamespaceURI().equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE),
                "1.1 ns equals URI_NS_SOAP_ENVELOPE");
        check(env12.getNamespaceURI().equals(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE),
                "1.2 ns equals URI_NS_SOAP_1_2_ENVELOPE");
        // 1.2 结构与 1.1 对称
        check(env12.getHeader() != null, "1.2 envelope has header");
        check(env12.getBody() != null, "1.2 envelope has body");
        check("Envelope".equals(env12.getElementName().getLocalName()), "1.2 envelope local name");
        check(env12.getHeader().getParentNode() == env12, "1.2 header parent is envelope");
        check(env12.getBody().getParentNode() == env12, "1.2 body parent is envelope");
        // 1.2 上 addHeader 同样拒绝重复
        try {
            env12.addHeader();
            check(false, "1.2 addHeader duplicate should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "1.2 addHeader duplicate throws SOAPException");
        }
        // 1.2 createName 与 1.1 行为一致
        javax.xml.soap.Name z = env12.createName("z");
        check("z".equals(z.getQualifiedName()) && "".equals(z.getURI()), "1.2 createName(local) same semantics");
    }
}
