/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMSignatureMethod conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMSignatureMethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMSignatureMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignatureMethod", false, DOMSignatureMethodTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMSignatureMethod"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.AbstractDOMSignatureMethod"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getParameterSpec"), "method:getParameterSpec");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignatureMethod");
        check("org.jcp.xml.dsig.internal.dom.AbstractDOMSignatureMethod".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.AbstractDOMSignatureMethod");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public abstract]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getParameterSpec", "hashCode", "marshal"))),
                "exact public/protected method face (4 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.security.spec.AlgorithmParameterSpec,"), "constructor(AlgorithmParameterSpec) present");
        check(ctorFace.contains("org.w3c.dom.Element,"), "constructor(Element) present");
        // black-box behavior
        javax.xml.crypto.dsig.SignatureMethod sm = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM")
                .newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null);
        check(sm instanceof org.jcp.xml.dsig.internal.dom.DOMSignatureMethod,
                "factory RSA_SHA1 wiring is the DOMSignatureMethod family");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1.equals(sm.getAlgorithm()), "algorithm round trip");
        check(sm.getParameterSpec() == null, "no parameter spec by default");
        javax.xml.crypto.dsig.SignatureMethod dsa = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM")
                .newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.DSA_SHA1, null);
        check(dsa instanceof org.jcp.xml.dsig.internal.dom.DOMSignatureMethod
                && dsa != sm, "DSA_SHA1 also wired to the family, distinct instance");
    }
}
