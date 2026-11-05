/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMSignatureProperties conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMSignaturePropertiesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMSignaturePropertiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignatureProperties", false, DOMSignaturePropertiesTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMSignatureProperties"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.SignatureProperties"), "iface:javax.xml.crypto.dsig.SignatureProperties");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getProperties"), "method:getProperties");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignatureProperties");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.SignatureProperties"))), "exact interface set");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "modifier set [public final]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getId", "getProperties", "hashCode", "marshal"))),
                "exact public/protected method face (5 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.util.List,java.lang.String,"), "constructor(List, String) present");
        check(ctorFace.contains("org.w3c.dom.Element,"), "constructor(Element) present");
        // black-box behavior
        javax.xml.parsers.DocumentBuilder b = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = b.newDocument();
        org.w3c.dom.Element e = d.createElement("p");
        d.appendChild(e);
        try {
            new org.jcp.xml.dsig.internal.dom.DOMSignatureProperties(
                    java.util.Collections.<javax.xml.crypto.dsig.SignatureProperty>emptyList(), "sp1");
            check(false, "expected IAE for empty properties");
        } catch (IllegalArgumentException expected) {
            check("properties cannot be empty".equals(expected.getMessage()), "empty properties IAE message");
        }
        javax.xml.crypto.dom.DOMStructure content = new javax.xml.crypto.dom.DOMStructure(e);
        org.jcp.xml.dsig.internal.dom.DOMSignatureProperty one =
                new org.jcp.xml.dsig.internal.dom.DOMSignatureProperty(java.util.Arrays.asList(content), "t0", "p0");
        org.jcp.xml.dsig.internal.dom.DOMSignatureProperties sp =
                new org.jcp.xml.dsig.internal.dom.DOMSignatureProperties(java.util.Arrays.asList(one), "sp1");
        check("sp1".equals(sp.getId()), "getId round trip");
        check(sp.getProperties().size() == 1, "one property retained");
        check("t0".equals(sp.getProperties().get(0).getTarget()), "property forwarded unchanged");
        try {
            sp.getProperties().add((javax.xml.crypto.dsig.SignatureProperty) null);
            check(false, "expected UOE modifying properties");
        } catch (UnsupportedOperationException expected) {
            check(true, "properties list is unmodifiable");
        }
        check(sp instanceof javax.xml.crypto.dsig.SignatureProperties, "DOMSignatureProperties is a SignatureProperties");
    }
}
