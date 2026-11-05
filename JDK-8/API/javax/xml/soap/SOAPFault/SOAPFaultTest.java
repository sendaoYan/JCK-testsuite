/*
 * @test
 * @summary javax.xml.soap.SOAPFault conformance: interface hierarchy and methods.
 * @run main SOAPFaultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPFaultTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPFault");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPBodyElement"), "implements javax.xml.soap.SOAPBodyElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setFaultCode"), "has method setFaultCode");
        check(names.contains("getFaultCodeAsName"), "has method getFaultCodeAsName");
        check(names.contains("getFaultCodeAsQName"), "has method getFaultCodeAsQName");
        check(names.contains("getFaultSubcodes"), "has method getFaultSubcodes");
        check(names.contains("removeAllFaultSubcodes"), "has method removeAllFaultSubcodes");
        check(names.contains("appendFaultSubcode"), "has method appendFaultSubcode");
        check(names.contains("getFaultCode"), "has method getFaultCode");
        check(names.contains("setFaultActor"), "has method setFaultActor");
        check(names.contains("getFaultActor"), "has method getFaultActor");
        check(names.contains("setFaultString"), "has method setFaultString");
        check(names.contains("getFaultString"), "has method getFaultString");
        check(names.contains("getFaultStringLocale"), "has method getFaultStringLocale");
        check(names.contains("hasDetail"), "has method hasDetail");
        check(names.contains("getDetail"), "has method getDetail");
        check(names.contains("addDetail"), "has method addDetail");
        check(names.contains("getFaultReasonLocales"), "has method getFaultReasonLocales");
        check(names.contains("getFaultReasonTexts"), "has method getFaultReasonTexts");
        check(names.contains("getFaultReasonText"), "has method getFaultReasonText");
        check(names.contains("addFaultReasonText"), "has method addFaultReasonText");
        check(names.contains("getFaultNode"), "has method getFaultNode");
        check(names.contains("setFaultNode"), "has method setFaultNode");
        check(names.contains("getFaultRole"), "has method getFaultRole");
        check(names.contains("setFaultRole"), "has method setFaultRole");
        testFaultCode11();
        testFaultStringActor11();
        testDetailChannel();
        testFault11Unsupported12Apis();
        testFault12Subcodes();
        testFault12Reasons();
        testFault12RoleNode();
        testFault12Negative();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPFault freshFault11() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody().addFault();
    }

    static javax.xml.soap.SOAPFault freshFault12() throws Exception {
        return javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL)
                .createMessage().getSOAPBody().addFault();
    }

    static int count(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // 1.1 fault code：String/Name/QName 三通道回读
    static void testFaultCode11() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault11();
        f.setFaultCode("Client");
        check("Client".equals(f.getFaultCode()), "setFaultCode(String)/getFaultCode round-trip");
        javax.xml.soap.Name asName = f.getFaultCodeAsName();
        check(asName != null, "getFaultCodeAsName non-null");
        check("Client".equals(asName.getLocalName()), "code Name local part");
        check("".equals(asName.getPrefix()), "unqualified code Name prefix empty");
        check("".equals(asName.getURI()), "unqualified code Name uri empty");
        check(f.getFaultCodeAsQName().equals(new javax.xml.namespace.QName("Client")),
                "unqualified code QName");
        // Name 通道（env 限定）
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        String ns11 = "http://schemas.xmlsoap.org/soap/envelope/";
        javax.xml.soap.SOAPFault f2 = freshFault11();
        f2.setFaultCode(env.createName("VersionMismatch", "SOAP-ENV", ns11));
        check("SOAP-ENV:VersionMismatch".equals(f2.getFaultCode()), "setFaultCode(Name) qualified string form");
        check("SOAP-ENV:VersionMismatch".equals(f2.getFaultCodeAsName().getQualifiedName()),
                "code Name qualified name");
        check(f2.getFaultCodeAsQName().equals(new javax.xml.namespace.QName(ns11, "VersionMismatch")),
                "code QName ns round-trip");
        // QName 通道
        javax.xml.soap.SOAPFault f3 = freshFault11();
        f3.setFaultCode(new javax.xml.namespace.QName(ns11, "MustUnderstand"));
        check("MustUnderstand".equals(f3.getFaultCodeAsName().getLocalName()), "setFaultCode(QName) local part");
        check(f3.getFaultCodeAsQName().getNamespaceURI().equals(ns11), "setFaultCode(QName) ns");
    }

    // 1.1 fault string / locale / actor
    static void testFaultStringActor11() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault11();
        // RI 实测：无参 addFault 的默认 fault string 是占位文本而非 null
        check("Fault string, and possibly fault code, not set".equals(f.getFaultString()),
                "default fault string is RI placeholder");
        check(f.getFaultStringLocale() == null, "default fault string locale null");
        f.setFaultString("boom");
        check("boom".equals(f.getFaultString()), "setFaultString(String) round-trip");
        check(f.getFaultStringLocale() == null, "setFaultString(String) keeps locale null (javadoc: null locale)");
        f.setFaultString("boum", java.util.Locale.FRENCH);
        check("boum".equals(f.getFaultString()), "setFaultString(String,Locale) string");
        check(java.util.Locale.FRENCH.equals(f.getFaultStringLocale()), "setFaultString(String,Locale) locale");
        check(f.getFaultActor() == null, "default fault actor null");
        f.setFaultActor("http://actor/");
        check("http://actor/".equals(f.getFaultActor()), "setFaultActor round-trip");
        f.setFaultActor(null);
        check("http://actor/".equals(f.getFaultActor()), "setFaultActor(null) is a no-op in RI (value kept)");
    }

    // 1.1 detail 通道：hasDetail/getDetail/addDetail + entry 迭代
    @SuppressWarnings("unchecked")
    static void testDetailChannel() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault11();
        check(!f.hasDetail(), "no detail initially");
        check(f.getDetail() == null, "getDetail null initially");
        javax.xml.soap.Detail d = f.addDetail();
        check(d != null, "addDetail non-null");
        check(f.hasDetail(), "hasDetail true after addDetail");
        check(f.getDetail() == d, "getDetail same instance");
        try {
            f.addDetail();
            check(false, "second addDetail should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "second addDetail throws SOAPException");
        }
        javax.xml.soap.DetailEntry e1 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "e1", "dd"));
        javax.xml.soap.DetailEntry e2 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "e2", "dd"));
        e1.addTextNode("x");
        e2.addTextNode("y");
        check(count(d.getDetailEntries()) == 2, "two detail entries");
        check(e1.getParentElement() == d, "detail entry parent is detail");
        // 1.2 detail 同样可用
        javax.xml.soap.SOAPFault f12 = freshFault12();
        javax.xml.soap.Detail d12 = f12.addDetail();
        d12.addDetailEntry(new javax.xml.namespace.QName("http://d/", "z12"));
        check(f12.hasDetail() && count(d12.getDetailEntries()) == 1, "1.2 detail channel works");
    }

    // 1.1 上 1.2 专属 API → UnsupportedOperationException（非 SOAPException，实测锁定）
    static void testFault11Unsupported12Apis() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault11();
        try { f.appendFaultSubcode(new javax.xml.namespace.QName("http://s/", "sub")); check(false, "1.1 appendFaultSubcode should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("SOAP 1.1"), "1.1 appendFaultSubcode message: " + e.getMessage()); }
        try { java.util.Iterator i = f.getFaultSubcodes(); check(false, "1.1 getFaultSubcodes should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 getFaultSubcodes throws"); }
        try { f.removeAllFaultSubcodes(); check(false, "1.1 removeAllFaultSubcodes should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 removeAllFaultSubcodes throws"); }
        try { f.addFaultReasonText("r", java.util.Locale.ENGLISH); check(false, "1.1 addFaultReasonText should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("SOAP 1.1"), "1.1 addFaultReasonText message: " + e.getMessage()); }
        try { java.util.Iterator i = f.getFaultReasonTexts(); check(false, "1.1 getFaultReasonTexts should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 getFaultReasonTexts throws"); }
        try { java.util.Iterator i = f.getFaultReasonLocales(); check(false, "1.1 getFaultReasonLocales should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 getFaultReasonLocales throws"); }
        try { String s = f.getFaultReasonText(java.util.Locale.ENGLISH); check(false, "1.1 getFaultReasonText should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 getFaultReasonText throws"); }
        try { f.setFaultRole("r"); check(false, "1.1 setFaultRole should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 setFaultRole throws"); }
        try { String s = f.getFaultRole(); check(false, "1.1 getFaultRole should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 getFaultRole throws"); }
        try { f.setFaultNode("n"); check(false, "1.1 setFaultNode should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 setFaultNode throws"); }
        try { String s = f.getFaultNode(); check(false, "1.1 getFaultNode should throw"); }
        catch (java.lang.UnsupportedOperationException e) { check(true, "1.1 getFaultNode throws"); }
    }

    // 1.2 subcode 栈：appendFaultSubcode 顺序迭代 + removeAllFaultSubcodes 清空
    @SuppressWarnings("unchecked")
    static void testFault12Subcodes() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault12();
        check(count(f.getFaultSubcodes()) == 0, "no subcodes initially");
        f.appendFaultSubcode(new javax.xml.namespace.QName("http://s/", "sub1"));
        f.appendFaultSubcode(new javax.xml.namespace.QName("http://s/", "sub2"));
        java.util.Iterator it = f.getFaultSubcodes();
        boolean first = true;
        int n = 0;
        while (it.hasNext()) {
            javax.xml.namespace.QName q = (javax.xml.namespace.QName) it.next();
            n++;
            if (first) { check("sub1".equals(q.getLocalPart()), "subcodes iterate in append order (FIFO)"); first = false; }
        }
        check(n == 2, "two subcodes iterated");
        f.removeAllFaultSubcodes();
        check(count(f.getFaultSubcodes()) == 0, "subcodes cleared");
        // RI 实测锁定：removeAllFaultSubcodes 后再 appendFaultSubcode 不再生效（子码列表被永久破坏）
        f.appendFaultSubcode(new javax.xml.namespace.QName("http://s/", "again"));
        check(count(f.getFaultSubcodes()) == 0, "appendFaultSubcode is inert after removeAllFaultSubcodes (RI quirk)");
    }

    // 1.2 reason：多 Locale 添加/读取/枚举 + null locale 负例
    @SuppressWarnings("unchecked")
    static void testFault12Reasons() throws Exception {
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory
                .newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL)
                .createMessage().getSOAPBody()
                .addFault(new javax.xml.namespace.QName("http://www.w3.org/2003/05/soap-envelope", "Sender"), "base");
        int baseCount = count(f.getFaultReasonTexts());
        check(baseCount == 1, "addFault seeds one reason text");
        f.addFaultReasonText("r-en", java.util.Locale.ENGLISH);
        f.addFaultReasonText("r-fr", java.util.Locale.FRENCH);
        check("r-en".equals(f.getFaultReasonText(java.util.Locale.ENGLISH)), "reason text EN round-trip");
        check("r-fr".equals(f.getFaultReasonText(java.util.Locale.FRENCH)), "reason text FR round-trip");
        check(f.getFaultReasonText(java.util.Locale.CHINESE) == null, "unknown locale reason null");
        // 计数与默认 Locale 无关地锁定（若默认 Locale==ENGLISH 则 EN 添加为替换而非新增）
        int expected = baseCount + (java.util.Locale.getDefault().equals(java.util.Locale.ENGLISH) ? 1 : 2);
        check(count(f.getFaultReasonTexts()) == expected, "reason texts count includes additions (locale-aware)");
        java.util.Iterator locs = f.getFaultReasonLocales();
        boolean sawEn = false;
        int ln = 0;
        while (locs.hasNext()) {
            Object o = locs.next();
            check(o instanceof java.util.Locale, "reason locale is Locale");
            if (java.util.Locale.ENGLISH.equals(o)) sawEn = true;
            ln++;
        }
        check(ln == expected, "reason locales count matches");
        check(sawEn, "EN locale enumerated");
        // 同 locale 覆盖语义：再次添加替换文本且不增计数
        f.addFaultReasonText("r-en2", java.util.Locale.ENGLISH);
        check("r-en2".equals(f.getFaultReasonText(java.util.Locale.ENGLISH)), "re-add same locale replaces text");
        check(count(f.getFaultReasonTexts()) == expected, "re-add same locale keeps count");
        // getFaultString/getFaultStringLocale 与 reason 桥接（RI 实测：默认 locale reason）
        check(f.getFaultString() != null, "1.2 getFaultString backed by reason");
        // null locale → SOAPException
        try {
            f.addFaultReasonText("x", null);
            check(false, "addFaultReasonText with null locale should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("locale"),
                    "null locale message: " + e.getMessage());
        }
    }

    // 1.2 role/node
    static void testFault12RoleNode() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault12();
        check(f.getFaultRole() == null, "default fault role null");
        f.setFaultRole("http://role12/");
        check("http://role12/".equals(f.getFaultRole()), "setFaultRole round-trip");
        check(f.getFaultNode() == null, "default fault node null");
        f.setFaultNode("http://node12/");
        check("http://node12/".equals(f.getFaultNode()), "setFaultNode round-trip");
        // 默认无参 addFault → Receiver
        check(f.getFaultCodeAsQName().equals(
                new javax.xml.namespace.QName("http://www.w3.org/2003/05/soap-envelope", "Receiver")),
                "1.2 no-arg addFault code Receiver");
        check("Receiver".equals(f.getFaultCodeAsName().getLocalName()), "1.2 code as Name local part");
        check("http://www.w3.org/2003/05/soap-envelope".equals(f.getFaultCodeAsName().getURI()),
                "1.2 code as Name uri");
    }

    // 1.2 负例：setFaultCode(String) 非标准码 → SOAPException
    static void testFault12Negative() throws Exception {
        javax.xml.soap.SOAPFault f = freshFault12();
        try {
            f.setFaultCode("Client");
            check(false, "1.2 setFaultCode(String) unqualified should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("not a standard Code value"),
                    "1.2 setFaultCode(String) message: " + e.getMessage());
        }
        try {
            f.setFaultCode("Sender");
            check(false, "1.2 setFaultCode(String) bare local should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Sender"),
                    "1.2 setFaultCode('Sender') message contains name: " + e.getMessage());
        }
        // QName 通道正常
        f.setFaultCode(new javax.xml.namespace.QName("http://www.w3.org/2003/05/soap-envelope", "Sender"));
        check("Sender".equals(f.getFaultCodeAsQName().getLocalPart()), "1.2 setFaultCode(QName) works");
        // 1.2 默认 fault string 提示（RI 实测）
        javax.xml.soap.SOAPFault f2 = freshFault12();
        check("Fault string, and possibly fault code, not set".equals(f2.getFaultString()),
                "1.2 default fault string placeholder");
    }
}
