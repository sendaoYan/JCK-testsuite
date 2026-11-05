/*
 * @test
 * @summary javax.xml.soap.SOAPHeaderElement conformance: 1.1 真实通道 actor/mustUnderstand("1"/"0")回读+1.2 专属 API 全 UOE/1.2 setActor-setRole 同存储/mu-relay 布尔字面量/父链与 detach/examineAllHeaderElements.
 * @run main SOAPHeaderElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPHeaderElementTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPHeaderElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setActor"), "has method setActor");
        check(names.contains("setRole"), "has method setRole");
        check(names.contains("getActor"), "has method getActor");
        check(names.contains("getRole"), "has method getRole");
        check(names.contains("setMustUnderstand"), "has method setMustUnderstand");
        check(names.contains("getMustUnderstand"), "has method getMustUnderstand");
        check(names.contains("setRelay"), "has method setRelay");
        check(names.contains("getRelay"), "has method getRelay");
        check(c.getDeclaredMethods().length == 8, "exactly 8 own methods");
        testRealHeader11();
        testSoap11UnsupportedRoleRelay();
        testMustUnderstandLiterals();
        testSoap12HeaderElement();
        testSoap12SharedStorage();
        testParentAndDetach();
        testExamineAndValue();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPMessage msg11() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage();
    }

    static int count(java.util.Iterator<?> it) {
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        return n;
    }

    // 1.1 真实通道：addHeaderElement + actor 回读 + 文本 + 编码风格
    static void testRealHeader11() throws Exception {
        javax.xml.soap.SOAPMessage msg = msg11();
        javax.xml.soap.SOAPHeader header = msg.getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr", "h"));
        check(he != null, "addHeaderElement(QName) non-null");
        check(he instanceof javax.xml.soap.SOAPHeaderElement, "product is SOAPHeaderElement");
        check(he.getElementQName().equals(new javax.xml.namespace.QName("urn:h", "hdr", "h")),
                "header element QName round-trip");
        check(he.getActor() == null, "default actor null");
        check(!he.getMustUnderstand(), "default mustUnderstand false");
        he.setActor("urn:actor:a");
        check("urn:actor:a".equals(he.getActor()), "setActor/getActor round-trip");
        check("urn:actor:a".equals(he.getAttributeValue(
                new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE, "actor"))),
                "actor stored as soap:actor attribute");
        he.setActor("urn:actor:b");
        check("urn:actor:b".equals(he.getActor()), "setActor overwrite");
        // 文本与编码风格
        he.addTextNode("htext");
        check("htext".equals(he.getValue()), "header element text value");
        String enc = javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENCODING;
        he.setEncodingStyle(enc);
        check(enc.equals(he.getEncodingStyle()), "header element encoding style round-trip");
        // SOAPConstants 角色常量可直接作为 actor
        he.setActor(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT);
        check(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT.equals(he.getActor()),
                "URI_SOAP_ACTOR_NEXT usable as actor");
    }

    // 1.1 上 1.2 专属 API 全抛 UnsupportedOperationException（RI 精确消息实测）
    static void testSoap11UnsupportedRoleRelay() throws Exception {
        javax.xml.soap.SOAPHeader header = msg11().getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr2", "h"));
        try {
            he.getRole();
            check(false, "1.1 getRole must throw");
        } catch (java.lang.UnsupportedOperationException e) {
            check("Role not supported by SOAP 1.1".equals(e.getMessage()),
                    "1.1 getRole UOE message exact: " + e.getMessage());
        }
        try {
            he.setRole("urn:r");
            check(false, "1.1 setRole must throw");
        } catch (java.lang.UnsupportedOperationException e) {
            check("Role not supported by SOAP 1.1".equals(e.getMessage()), "1.1 setRole UOE message exact");
        }
        try {
            he.getRelay();
            check(false, "1.1 getRelay must throw");
        } catch (java.lang.UnsupportedOperationException e) {
            check("Relay not supported by SOAP 1.1".equals(e.getMessage()),
                    "1.1 getRelay UOE message exact: " + e.getMessage());
        }
        try {
            he.setRelay(true);
            check(false, "1.1 setRelay must throw");
        } catch (java.lang.UnsupportedOperationException e) {
            check("Relay not supported by SOAP 1.1".equals(e.getMessage()), "1.1 setRelay UOE message exact");
        }
        he.setActor("urn:actor:z"); // actor 本身不受影响
        check("urn:actor:z".equals(he.getActor()), "setActor unaffected by role/relay UOE");
    }

    // mustUnderstand DOM 字面量：1.1 用 "1"/"0"（版本差异：1.2 用 "true"/"false"）
    static void testMustUnderstandLiterals() throws Exception {
        javax.xml.soap.SOAPHeader header = msg11().getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr3", "h"));
        javax.xml.namespace.QName muAttr =
                new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE, "mustUnderstand");
        he.setMustUnderstand(true);
        check(he.getMustUnderstand(), "MU true readback");
        check("1".equals(he.getAttributeValue(muAttr)), "SOAP 1.1 MU literal is \"1\"");
        he.setMustUnderstand(false);
        check(!he.getMustUnderstand(), "MU false readback");
        check("0".equals(he.getAttributeValue(muAttr)), "SOAP 1.1 MU literal is \"0\"");
        he.setMustUnderstand(true);
        check(he.getMustUnderstand(), "MU toggle back true");
    }

    // 1.2 真实通道：role/relay/MU/actor 全回读，字面量 "true"/"false"
    static void testSoap12HeaderElement() throws Exception {
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        javax.xml.soap.SOAPHeader header = msg.getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr12", "h"));
        check(he != null, "1.2 addHeaderElement non-null");
        check(he.getRole() == null, "1.2 default role null");
        check(he.getActor() == null, "1.2 default actor null");
        check(!he.getMustUnderstand(), "1.2 default MU false");
        check(!he.getRelay(), "1.2 default relay false");
        he.setRole("urn:role:12");
        check("urn:role:12".equals(he.getRole()), "1.2 setRole/getRole round-trip");
        javax.xml.namespace.QName roleAttr =
                new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "role");
        check("urn:role:12".equals(he.getAttributeValue(roleAttr)), "1.2 role stored as env:role attribute");
        he.setRelay(true);
        check(he.getRelay(), "1.2 setRelay(true) round-trip");
        he.setRelay(false);
        check(!he.getRelay(), "1.2 setRelay(false) round-trip");
        javax.xml.namespace.QName muAttr12 =
                new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "mustUnderstand");
        javax.xml.namespace.QName relayAttr12 =
                new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "relay");
        he.setMustUnderstand(true);
        check("true".equals(he.getAttributeValue(muAttr12)), "1.2 MU literal is \"true\"");
        he.setRelay(true);
        check("true".equals(he.getAttributeValue(relayAttr12)), "1.2 relay literal is \"true\"");
        // 角色常量
        he.setRole(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        check(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT.equals(he.getRole()),
                "URI_SOAP_1_2_ROLE_NEXT usable as role");
        check(!javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT.equals(
                javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NONE),
                "role NEXT and NONE constants distinct");
        check(!javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER
                .equals(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT),
                "role ULTIMATE_RECEIVER distinct from NEXT");
    }

    // 1.2 上 setActor 与 setRole 同一存储（RI 实测互写互读）
    static void testSoap12SharedStorage() throws Exception {
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        javax.xml.soap.SOAPHeaderElement he = msg.getSOAPHeader().addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr12b", "h"));
        he.setActor("urn:via-actor");
        check("urn:via-actor".equals(he.getRole()), "1.2 setActor visible through getRole (shared storage)");
        he.setRole("urn:via-role");
        check("urn:via-role".equals(he.getActor()), "1.2 setRole visible through getActor (shared storage)");
        // setActor 在 1.2 上不抛（别名语义）
        check(true, "1.2 setActor does not throw (alias of setRole)");
    }

    // 父链（SAAJ+DOM）与 detach 生命周期
    static void testParentAndDetach() throws Exception {
        javax.xml.soap.SOAPMessage msg = msg11();
        javax.xml.soap.SOAPHeader header = msg.getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement he = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr4", "h"));
        check(he.getParentElement() == header, "SAAJ parent is header");
        check(he.getParentNode() == header, "DOM parent is header");
        check(count(header.getChildElements()) == 1, "header child count 1");
        he.detachNode();
        check(count(header.getChildElements()) == 0, "header child count 0 after detach");
        check(he.getParentElement() == null, "detached header element parent null");
        // 再挂一个不受影响
        javax.xml.soap.SOAPHeaderElement he2 = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr5", "h"));
        check(count(header.getChildElements()) == 1, "header reusable after detach");
        he2.detachNode();
        he2.detachNode();
        check(true, "double detachNode does not throw");
    }

    // examineAllHeaderElements 视角 + setValue 回读
    static void testExamineAndValue() throws Exception {
        javax.xml.soap.SOAPMessage msg = msg11();
        javax.xml.soap.SOAPHeader header = msg.getSOAPHeader();
        javax.xml.soap.SOAPHeaderElement target = header.addHeaderElement(
                new javax.xml.namespace.QName("urn:h", "hdr6", "h"));
        header.addHeaderElement(new javax.xml.namespace.QName("urn:h", "hdr7", "h"));
        java.util.Iterator<?> it = header.examineAllHeaderElements();
        int n = 0;
        boolean saw = false;
        while (it.hasNext()) {
            Object o = it.next();
            n++;
            if (o == target) saw = true;
        }
        check(n == 2, "examineAllHeaderElements sees both elements");
        check(saw, "examineAllHeaderElements yields same instance");
        target.setValue("hv");
        check("hv".equals(target.getValue()), "header element setValue/getValue");
        check("hv".equals(target.getTextContent()), "header element textContent");
    }
}
