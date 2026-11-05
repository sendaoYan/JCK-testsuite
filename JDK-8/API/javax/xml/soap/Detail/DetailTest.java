/*
 * @test
 * @summary javax.xml.soap.Detail conformance: interface hierarchy and methods.
 * @run main DetailTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DetailTest {
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
        Class<?> c = Class.forName("javax.xml.soap.Detail");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPFaultElement"), "implements javax.xml.soap.SOAPFaultElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addDetailEntry"), "has method addDetailEntry");
        check(names.contains("getDetailEntries"), "has method getDetailEntries");
        testDetailChannel();
        testEntryLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.Detail newDetail() throws Exception {
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        return f.addDetail();
    }

    static int count(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // 真实 fault.addDetail 通道：多条 addDetailEntry 迭代计数 + instanceof + Name/QName 双形态
    @SuppressWarnings("unchecked")
    static void testDetailChannel() throws Exception {
        javax.xml.soap.Detail d = newDetail();
        check(count(d.getDetailEntries()) == 0, "fresh detail has no entries");
        javax.xml.soap.DetailEntry de1 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "e1", "dd"));
        check(de1 != null, "addDetailEntry(QName) non-null");
        check(de1 instanceof javax.xml.soap.DetailEntry, "returns DetailEntry");
        check(de1 instanceof javax.xml.soap.SOAPElement, "DetailEntry is a SOAPElement");
        check(de1.getParentElement() == d, "entry parent is detail");
        javax.xml.soap.DetailEntry de2 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "e2", "dd"));
        javax.xml.soap.DetailEntry de3 = d.addDetailEntry(new javax.xml.namespace.QName("http://d2/", "e3"));
        check(count(d.getDetailEntries()) == 3, "three entries iterated");
        // 迭代元素逐条 instanceof DetailEntry
        java.util.Iterator it = d.getDetailEntries();
        int n = 0;
        boolean allEntries = true;
        while (it.hasNext()) {
            Object o = it.next();
            if (!(o instanceof javax.xml.soap.DetailEntry)) allEntries = false;
            n++;
        }
        check(n == 3, "iterator yields 3");
        check(allEntries, "all iterated objects are DetailEntry");
        // Name 形态
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.Detail d2 = newDetail();
        javax.xml.soap.DetailEntry deN = d2.addDetailEntry(env.createName("ne", "nx", "http://n/"));
        check("ne".equals(deN.getElementName().getLocalName()), "addDetailEntry(Name) local");
        check("http://n/".equals(deN.getElementQName().getNamespaceURI()), "addDetailEntry(Name) uri");
        // detail 本身的身份
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        javax.xml.soap.Detail dd = f.addDetail();
        check(dd instanceof javax.xml.soap.SOAPFaultElement, "Detail is a SOAPFaultElement");
        check(f.getDetail() == dd, "fault.getDetail returns same detail");
        check(dd.getParentElement() == f, "detail parent is fault");
        // entry QName 精确回读
        check(de1.getElementQName().equals(new javax.xml.namespace.QName("http://d/", "e1", "dd")),
                "entry QName round-trip");
    }

    // entry 生命周期：detachNode 减员；fault 端 hasDetail/getDetail 联动
    @SuppressWarnings("unchecked")
    static void testEntryLifecycle() throws Exception {
        javax.xml.soap.SOAPFault f = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPBody().addFault();
        javax.xml.soap.Detail d = f.addDetail();
        javax.xml.soap.DetailEntry e1 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "x1"));
        javax.xml.soap.DetailEntry e2 = d.addDetailEntry(new javax.xml.namespace.QName("http://d/", "x2"));
        e1.addTextNode("tv1");
        check("tv1".equals(e1.getValue()), "entry text value round-trip");
        check(d.getTextContent().contains("tv1"), "detail textContent includes entry text");
        e1.detachNode();
        check(count(d.getDetailEntries()) == 1, "entry detached shrinks iteration");
        check(e2.getParentElement() == d, "remaining entry keeps parent");
        // detach 整个 detail → fault.hasDetail 翻转
        d.detachNode();
        check(!f.hasDetail(), "hasDetail false after detail detach");
        check(f.getDetail() == null, "getDetail null after detail detach");
        // 可重建
        javax.xml.soap.Detail again = f.addDetail();
        check(f.hasDetail(), "addDetail works again after detach");
        check(again != d, "new detail instance distinct");
    }
}
