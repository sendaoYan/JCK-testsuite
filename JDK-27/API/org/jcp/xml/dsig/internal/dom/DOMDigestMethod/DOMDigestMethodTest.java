/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMDigestMethod conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMDigestMethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMDigestMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMDigestMethod", false, DOMDigestMethodTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMDigestMethod"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.DigestMethod"), "iface:javax.xml.crypto.dsig.DigestMethod");
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
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMDigestMethod");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.DigestMethod"))), "exact interface set");
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
        javax.xml.crypto.dsig.DigestMethod dm = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM")
                .newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        check(dm instanceof org.jcp.xml.dsig.internal.dom.DOMDigestMethod,
                "factory SHA256 wiring is the DOMDigestMethod family");
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals(dm.getAlgorithm()), "algorithm round trip");
        check(dm.getParameterSpec() == null, "no parameter spec for SHA256");
        check(dm instanceof javax.xml.crypto.AlgorithmMethod, "is an AlgorithmMethod");
        javax.xml.crypto.dsig.DigestMethod dm2 = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM")
                .newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA512, null);
        check(dm2 instanceof org.jcp.xml.dsig.internal.dom.DOMDigestMethod
                && dm2 != dm, "SHA512 also wired to the family, distinct instance");
    }
}
