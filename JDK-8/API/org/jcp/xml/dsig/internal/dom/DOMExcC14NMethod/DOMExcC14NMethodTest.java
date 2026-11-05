/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod conformance: type shape via reflection (headless-safe).
 * @run main DOMExcC14NMethodTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMExcC14NMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod", false, DOMExcC14NMethodTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer"), "superclass org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer");
        check(hasMethod(c, "getParameterSpecPrefixList"), "declares getParameterSpecPrefixList");
        check(hasMethod(c, "getParamsNSURI"), "declares getParamsNSURI");
        check(hasMethod(c, "init"), "declares init");
        check(hasMethod(c, "marshalParams"), "declares marshalParams");
        check(hasMethod(c, "transform"), "declares transform");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod");
        check("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getParameterSpecPrefixList", "getParamsNSURI", "init", "marshalParams", "transform"))),
                "exact public/protected method face (5 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains(""), "constructor(no-arg) present");
        // black-box behavior
        org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod t = new org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod();
        check(t.getAlgorithm() == null, "algorithm null before init");
        check(t.getProvider() == null, "provider null before init");
        check(t.getParameterSpec() == null, "parameter spec null before init");
        t.init((javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        check(t.getParameterSpec() == null, "init(null) accepted (no parameters required)");
        try {
            t.init((javax.xml.crypto.dsig.spec.TransformParameterSpec) new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(java.util.Collections.<String>emptyList()));
            check(true, "ExcC14N parameter spec accepted");
        } catch (java.security.InvalidAlgorithmParameterException unexpected) {
            check(false, "ExcC14N spec must be accepted: " + unexpected);
        }
        try {
            t.isFeatureSupported(null);
            check(false, "expected NPE for isFeatureSupported(null)");
        } catch (NullPointerException expected) {
            check(true, "isFeatureSupported(null) -> NPE");
        }
        check(!t.isFeatureSupported("dom"), "unknown feature reported unsupported");
        try {
            t.marshalParams(null, null);
            check(false, "expected NPE for marshalParams(null, null)");
        } catch (NullPointerException expected) {
            check(true, "marshalParams(null, null) -> NPE");
        }
        check(t instanceof javax.xml.crypto.dsig.TransformService, "DOMExcC14NMethod is a TransformService");
    }
}
