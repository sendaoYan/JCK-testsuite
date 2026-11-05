/*
 * @test
 * @summary javax.xml.soap.SOAPHeader conformance: interface hierarchy and methods.
 * @run main SOAPHeaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPHeaderTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPHeader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addHeaderElement"), "has method addHeaderElement");
        check(names.contains("examineMustUnderstandHeaderElements"), "has method examineMustUnderstandHeaderElements");
        check(names.contains("examineHeaderElements"), "has method examineHeaderElements");
        check(names.contains("extractHeaderElements"), "has method extractHeaderElements");
        check(names.contains("addNotUnderstoodHeaderElement"), "has method addNotUnderstoodHeaderElement");
        check(names.contains("addUpgradeHeaderElement"), "has method addUpgradeHeaderElement");
        check(names.contains("examineAllHeaderElements"), "has method examineAllHeaderElements");
        check(names.contains("extractAllHeaderElements"), "has method extractAllHeaderElements");
        testAddHeaderElements();
        testExamineExtract();
        testMustUnderstand();
        testActorRole11();
        testActorRole12();
        testNotUnderstoodUpgrade();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPHeader header11() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPHeader();
    }

    static int count(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // addHeaderElement(QName)/(Name) 回读 + 类型
    @SuppressWarnings("unchecked")
    static void testAddHeaderElements() throws Exception {
        javax.xml.soap.SOAPHeader h = header11();
        javax.xml.soap.SOAPHeaderElement he1 = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "t1", "hh"));
        check(he1 != null, "addHeaderElement(QName) non-null");
        check(he1.getElementQName().equals(new javax.xml.namespace.QName("http://h/", "t1", "hh")),
                "addHeaderElement(QName) name round-trip");
        check(he1 instanceof javax.xml.soap.SOAPHeaderElement, "returns SOAPHeaderElement");
        check(he1.getParentElement() == h, "header element parent is header");
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeaderElement he2 = env.getHeader().addHeaderElement(env.createName("t2", "hh", "http://h/"));
        check("t2".equals(he2.getElementName().getLocalName()), "addHeaderElement(Name) local name");
        check("hh".equals(he2.getElementName().getPrefix()), "addHeaderElement(Name) prefix");
        check(count(h.examineAllHeaderElements()) == 1, "header holds first element");
        he1.addTextNode("hv1");
        check(h.getElementsByTagNameNS("http://h/", "t1").getLength() == 1, "DOM lookup header element");
    }

    // examine（不删除）与 extract（移除）语义：actor 过滤
    @SuppressWarnings("unchecked")
    static void testExamineExtract() throws Exception {
        javax.xml.soap.SOAPHeader h = header11();
        javax.xml.soap.SOAPHeaderElement a1 = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "one"));
        javax.xml.soap.SOAPHeaderElement a2 = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "two"));
        a1.setActor("http://actor/next");
        a2.setActor("http://actor/other");
        check(count(h.examineAllHeaderElements()) == 2, "examineAll sees 2");
        check(count(h.examineAllHeaderElements()) == 2, "examineAll does not remove");
        check(count(h.examineHeaderElements("http://actor/next")) == 1, "examine by actor next == 1");
        check(count(h.examineHeaderElements("http://actor/other")) == 1, "examine by actor other == 1");
        check(count(h.examineHeaderElements("http://none/")) == 0, "examine by unknown actor == 0");
        check(count(h.examineAllHeaderElements()) == 2, "examine(filtered) does not remove");
        // extractAll 移除全部
        check(count(h.extractAllHeaderElements()) == 2, "extractAll returns 2");
        check(count(h.examineAllHeaderElements()) == 0, "header empty after extractAll");
        // extract(actor) 只移除匹配
        javax.xml.soap.SOAPHeader h2 = header11();
        javax.xml.soap.SOAPHeaderElement b1 = h2.addHeaderElement(new javax.xml.namespace.QName("http://h/", "x1"));
        javax.xml.soap.SOAPHeaderElement b2 = h2.addHeaderElement(new javax.xml.namespace.QName("http://h/", "x2"));
        b1.setActor(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT);
        check(count(h2.extractHeaderElements(javax.xml.soap.SOAPConstants.URI_SOAP_ACTOR_NEXT)) == 1,
                "extract by NEXT actor == 1");
        check(count(h2.examineAllHeaderElements()) == 1, "only matching element extracted");
        // 迭代元素类型
        java.util.Iterator it = h2.examineAllHeaderElements();
        check(it.next() instanceof javax.xml.soap.SOAPHeaderElement, "iterator yields SOAPHeaderElement");
    }

    // setMustUnderstand/getMustUnderstand + examineMustUnderstandHeaderElements
    @SuppressWarnings("unchecked")
    static void testMustUnderstand() throws Exception {
        javax.xml.soap.SOAPHeader h = header11();
        javax.xml.soap.SOAPHeaderElement mu = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "mu"));
        javax.xml.soap.SOAPHeaderElement nmu = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "nmu"));
        check(!mu.getMustUnderstand(), "default mustUnderstand false");
        mu.setMustUnderstand(true);
        check(mu.getMustUnderstand(), "setMustUnderstand(true) round-trip");
        mu.setMustUnderstand(false);
        check(!mu.getMustUnderstand(), "setMustUnderstand(false) round-trip");
        mu.setMustUnderstand(true);
        nmu.setMustUnderstand(false);
        check(count(h.examineMustUnderstandHeaderElements(null)) == 1,
                "examineMustUnderstand filters to mustUnderstand elements");
        check(count(h.examineAllHeaderElements()) == 2, "both still present");
    }

    // 1.1：setActor/getActor 回读；getRole/setRole → UnsupportedOperationException
    static void testActorRole11() throws Exception {
        javax.xml.soap.SOAPHeader h = header11();
        javax.xml.soap.SOAPHeaderElement he = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "act"));
        check(he.getActor() == null, "default actor null");
        he.setActor("http://actor/mine");
        check("http://actor/mine".equals(he.getActor()), "setActor/getActor round-trip");
        try {
            he.getRole();
            check(false, "1.1 getRole should throw UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException e) {
            check(e.getMessage() != null && e.getMessage().contains("Role"),
                    "1.1 getRole message mentions Role: " + e.getMessage());
        }
        try {
            he.setRole("http://r/");
            check(false, "1.1 setRole should throw UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException e) {
            check(true, "1.1 setRole throws UnsupportedOperationException");
        }
        try {
            he.setRelay(true);
            check(false, "1.1 setRelay should throw UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException e) {
            check(e.getMessage() != null && e.getMessage().contains("Relay"),
                    "1.1 setRelay message mentions Relay: " + e.getMessage());
        }
        try {
            he.getRelay();
            check(false, "1.1 getRelay should throw UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException e) {
            check(true, "1.1 getRelay throws UnsupportedOperationException");
        }
    }

    // 1.2：setRole/getRole 回读；setActor 实测不抛（与 role 同一属性）；setRelay/getRelay 回读
    static void testActorRole12() throws Exception {
        javax.xml.soap.MessageFactory mf12 = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        javax.xml.soap.SOAPEnvelope env = mf12.createMessage().getSOAPPart().getEnvelope();
        if (env.getHeader() != null) env.getHeader().detachNode();
        javax.xml.soap.SOAPHeader h = env.addHeader();
        javax.xml.soap.SOAPHeaderElement he = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "t12"));
        he.setRole("http://r12/");
        check("http://r12/".equals(he.getRole()), "1.2 setRole/getRole round-trip");
        // RI 实测：1.2 上 setActor 不抛，写的就是 role 属性（javadoc 未禁止——锁定实际）
        he.setActor("http://a12/");
        check("http://a12/".equals(he.getActor()), "1.2 setActor/getActor round-trip");
        check("http://a12/".equals(he.getRole()), "1.2 actor and role share storage");
        he.setRole(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        check(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT.equals(he.getRole()), "1.2 role NEXT constant usable");
        check(!he.getRelay(), "1.2 default relay false");
        he.setRelay(true);
        check(he.getRelay(), "1.2 setRelay(true) round-trip");
        he.setRelay(false);
        check(!he.getRelay(), "1.2 setRelay(false) round-trip");
        // 1.2 role 过滤
        javax.xml.soap.SOAPHeaderElement he2 = h.addHeaderElement(new javax.xml.namespace.QName("http://h/", "t12b"));
        he2.setRole("http://none-r/");
        check(count(h.examineHeaderElements(javax.xml.soap.SOAPConstants.URI_SOAP_1_2_ROLE_NEXT)) == 1,
                "1.2 examine by role NEXT == 1");
        check(count(h.examineHeaderElements("http://none-r/")) == 1, "1.2 examine by other role == 1");
    }

    // 1.1：addNotUnderstood → UOE；addUpgradeHeaderElement 可用（生成 env:Upgrade）
    // 1.2：addNotUnderstoodHeaderElement 可用（生成 env:NotUnderstood）
    @SuppressWarnings("unchecked")
    static void testNotUnderstoodUpgrade() throws Exception {
        javax.xml.soap.SOAPHeader h11 = header11();
        try {
            h11.addNotUnderstoodHeaderElement(new javax.xml.namespace.QName("http://nu/", "nuq"));
            check(false, "1.1 addNotUnderstoodHeaderElement should throw UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException e) {
            check(e.getMessage() != null && e.getMessage().contains("SOAP 1.1"),
                    "1.1 addNotUnderstood message mentions SOAP 1.1");
        }
        javax.xml.soap.SOAPHeaderElement up = h11.addUpgradeHeaderElement("http://ns1/");
        check(up != null, "addUpgradeHeaderElement(String) non-null");
        check("Upgrade".equals(up.getElementName().getLocalName()), "upgrade element local name Upgrade");
        check("http://schemas.xmlsoap.org/soap/envelope/".equals(up.getElementQName().getNamespaceURI()),
                "upgrade element in soap 1.1 ns");
        javax.xml.soap.SOAPHeaderElement up2 = h11.addUpgradeHeaderElement(
                new java.util.ArrayList(java.util.Arrays.asList("http://ns2/", "http://ns3/")).iterator());
        check(up2 != null, "addUpgradeHeaderElement(Iterator) non-null");
        javax.xml.soap.SOAPHeaderElement up3 = h11.addUpgradeHeaderElement(new String[]{"http://ns4/"});
        check(up3 != null, "addUpgradeHeaderElement(String[]) non-null");
        // 1.2 addNotUnderstoodHeaderElement 成功
        javax.xml.soap.MessageFactory mf12 = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        javax.xml.soap.SOAPEnvelope env12 = mf12.createMessage().getSOAPPart().getEnvelope();
        if (env12.getHeader() != null) env12.getHeader().detachNode();
        javax.xml.soap.SOAPHeader h12 = env12.addHeader();
        javax.xml.soap.SOAPHeaderElement nu = h12.addNotUnderstoodHeaderElement(
                new javax.xml.namespace.QName("http://nu/", "nn"));
        check("NotUnderstood".equals(nu.getElementName().getLocalName()), "1.2 NotUnderstood element name");
        check("http://www.w3.org/2003/05/soap-envelope".equals(nu.getElementQName().getNamespaceURI()),
                "1.2 NotUnderstood in soap 1.2 ns");
    }
}
