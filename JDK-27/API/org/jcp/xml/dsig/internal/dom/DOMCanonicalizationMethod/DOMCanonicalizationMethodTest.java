/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMCanonicalizationMethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMCanonicalizationMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod", false, DOMCanonicalizationMethodTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMTransform"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.CanonicalizationMethod"), "iface:javax.xml.crypto.dsig.CanonicalizationMethod");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod");
        check("org.jcp.xml.dsig.internal.dom.DOMTransform".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMTransform");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.CanonicalizationMethod"))), "exact interface set");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("canonicalize", "equals", "hashCode"))),
                "exact public/protected method face (3 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("javax.xml.crypto.dsig.TransformService,"), "constructor(TransformService) present");
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        // black-box behavior
        javax.xml.crypto.dsig.TransformService c14n =
                javax.xml.crypto.dsig.TransformService.getInstance(
                        javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, "DOM");
        check(c14n instanceof org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod,
                "registered INCLUSIVE service is the DOM implementation");
        org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod dcm =
                new org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod(c14n);
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals(dcm.getAlgorithm()),
                "wrapped algorithm forwards");
        check(dcm.getParameterSpec() == null, "wrapped default c14n carries no parameters");
        check(dcm instanceof javax.xml.crypto.dsig.CanonicalizationMethod,
                "DOMCanonicalizationMethod is a CanonicalizationMethod");
        check(dcm instanceof javax.xml.crypto.dsig.Transform, "also a Transform");
    }
}
