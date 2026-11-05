/*
 * @test
 * @summary javax.xml.soap.SOAPFaultElement conformance: 真实 fault 子元素(faultcode/faultstring)类型与值锁定/Detail extends SOAPFaultElement 而 DetailEntry 不是/faultcode 子元素编码风格/detach 语义.
 * @run main SOAPFaultElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPFaultElementTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPFaultElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        check(c.getDeclaredMethods().length == 0, "marker interface: typing only, no own methods");
        testFaultChildrenTyping();
        testFaultStringRoundTrip();
        testEncodingStyleOnFaultChild();
        testDetailHierarchy();
        testFaultNotFaultElement();
        testDetachFaultChild();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPFault fault11() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody()
                .addFault(new javax.xml.namespace.QName(
                        javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE, "Client"), "fs");
    }

    static int count(java.util.Iterator<?> it) {
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        return n;
    }

    // 真实通道：fault 的 faultcode/faultstring 子元素均为 SOAPFaultElement（RI 实测）且值精确
    static void testFaultChildrenTyping() throws Exception {
        javax.xml.soap.SOAPFault fault = fault11();
        java.util.Iterator<?> it = fault.getChildElements();
        int faultElems = 0, all = 0;
        String codeVal = null, strVal = null;
        while (it.hasNext()) {
            Object o = it.next();
            all++;
            if (o instanceof javax.xml.soap.SOAPFaultElement) {
                faultElems++;
                javax.xml.soap.SOAPElement se = (javax.xml.soap.SOAPElement) o;
                if ("faultcode".equals(se.getLocalName())) codeVal = se.getValue();
                if ("faultstring".equals(se.getLocalName())) strVal = se.getValue();
            }
        }
        check(all == 2, "fault has exactly 2 child elements (faultcode+faultstring)");
        check(faultElems == 2, "both fault children are SOAPFaultElement (RI locked)");
        check("SOAP-ENV:Client".equals(codeVal), "faultcode child value is prefixed code form: " + codeVal);
        check("fs".equals(strVal), "faultstring child value exact");
    }

    // getFaultString/setFaultString 回读（经 fault 通道联动子元素）
    static void testFaultStringRoundTrip() throws Exception {
        javax.xml.soap.SOAPFault fault = fault11();
        check("fs".equals(fault.getFaultString()), "getFaultString initial");
        fault.setFaultString("fs2");
        check("fs2".equals(fault.getFaultString()), "setFaultString round-trip");
        // 子元素值同步更新
        String childVal = null;
        java.util.Iterator<?> it = fault.getChildElements();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof javax.xml.soap.SOAPFaultElement
                    && "faultstring".equals(((javax.xml.soap.SOAPElement) o).getLocalName())) {
                childVal = ((javax.xml.soap.SOAPElement) o).getValue();
            }
        }
        check("fs2".equals(childVal), "faultstring SOAPFaultElement child value syncs with setFaultString");
        // RI 实测：无 Locale 的 addFault/setFaultString → getFaultStringLocale()==null；带 Locale 形态才设置
        check(fault.getFaultStringLocale() == null,
                "fault string locale null when constructed without locale");
        fault.setFaultString("fs3", java.util.Locale.CANADA);
        check("fs3".equals(fault.getFaultString()), "setFaultString(String,Locale) string");
        check(java.util.Locale.CANADA.equals(fault.getFaultStringLocale()),
                "setFaultString(String,Locale) locale round-trip");
    }

    // faultcode 子元素（SOAPFaultElement）上的编码风格回读
    static void testEncodingStyleOnFaultChild() throws Exception {
        javax.xml.soap.SOAPFault fault = fault11();
        javax.xml.soap.SOAPElement codeChild = null;
        java.util.Iterator<?> it = fault.getChildElements();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof javax.xml.soap.SOAPFaultElement
                    && "faultcode".equals(((javax.xml.soap.SOAPElement) o).getLocalName())) {
                codeChild = (javax.xml.soap.SOAPElement) o;
            }
        }
        check(codeChild != null, "faultcode child found");
        check(codeChild.getEncodingStyle() == null, "fresh faultcode child no encoding style");
        String enc = javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENCODING;
        codeChild.setEncodingStyle(enc);
        check(enc.equals(codeChild.getEncodingStyle()), "faultcode child encoding style round-trip");
        check(codeChild.getParentElement() == fault, "faultcode child parent is fault");
    }

    // Detail extends SOAPFaultElement；DetailEntry extends SOAPElement（不是 FaultElement）——规范层次锁定
    static void testDetailHierarchy() throws Exception {
        javax.xml.soap.SOAPFault fault = fault11();
        javax.xml.soap.Detail d = fault.addDetail();
        check(fault.getDetail() == d, "getDetail returns same instance after addDetail");
        check(d instanceof javax.xml.soap.SOAPFaultElement,
                "Detail IS a SOAPFaultElement (interface fact)");
        check(d instanceof javax.xml.soap.SOAPElement, "Detail is SOAPElement");
        javax.xml.soap.DetailEntry de = d.addDetailEntry(
                new javax.xml.namespace.QName("urn:d", "de", "d"));
        check(de != null, "addDetailEntry non-null");
        check(de instanceof javax.xml.soap.SOAPElement, "DetailEntry is SOAPElement");
        check(!(de instanceof javax.xml.soap.SOAPFaultElement),
                "DetailEntry is NOT a SOAPFaultElement (extends SOAPElement only)");
        check(count(d.getDetailEntries()) == 1, "one detail entry iterated");
        check(de.getParentElement() == d, "detail entry parent is detail");
        check(fault.hasDetail(), "hasDetail true");
    }

    // SOAPFault 自身是 SOAPBodyElement 而非 SOAPFaultElement（RI ClassCastException 实测依据）
    static void testFaultNotFaultElement() throws Exception {
        javax.xml.soap.SOAPFault fault = fault11();
        check(!(fault instanceof javax.xml.soap.SOAPFaultElement),
                "SOAPFault object itself is NOT a SOAPFaultElement");
        check(fault instanceof javax.xml.soap.SOAPBodyElement,
                "SOAPFault IS a SOAPBodyElement");
        javax.xml.soap.SOAPBody body = (javax.xml.soap.SOAPBody) fault.getParentElement();
        check(body != null && body.hasFault(), "fault parent body hasFault");
    }

    // detach faultstring 子元素：剩余计数/hasFault 保持
    static void testDetachFaultChild() throws Exception {
        javax.xml.soap.SOAPFault fault = fault11();
        javax.xml.soap.SOAPElement strChild = null;
        java.util.Iterator<?> it = fault.getChildElements();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof javax.xml.soap.SOAPFaultElement
                    && "faultstring".equals(((javax.xml.soap.SOAPElement) o).getLocalName())) {
                strChild = (javax.xml.soap.SOAPElement) o;
            }
        }
        strChild.detachNode();
        check(count(fault.getChildElements()) == 1, "fault child count 1 after detaching faultstring");
        check(strChild.getParentElement() == null, "detached fault child parent null");
        javax.xml.soap.SOAPBody body = (javax.xml.soap.SOAPBody) fault.getParentElement();
        check(body.hasFault(), "hasFault still true after child detach");
    }
}
