/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMBase64Transform conformance: type shape via reflection (headless-safe).
 * @run main DOMBase64TransformTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMBase64TransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMBase64Transform", false, DOMBase64TransformTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.ApacheTransform"), "superclass org.jcp.xml.dsig.internal.dom.ApacheTransform");
        check(hasMethod(c, "init"), "declares init");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMBase64Transform");
        check("org.jcp.xml.dsig.internal.dom.ApacheTransform".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.ApacheTransform");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("init"))),
                "exact public/protected method face (1 names): " + names);
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
        org.jcp.xml.dsig.internal.dom.DOMBase64Transform t = new org.jcp.xml.dsig.internal.dom.DOMBase64Transform();
        check(t.getAlgorithm() == null, "algorithm null before init");
        check(t.getProvider() == null, "provider null before init");
        check(t.getParameterSpec() == null, "parameter spec null before init");
        t.init((javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        check(t.getParameterSpec() == null, "init(null) accepted (no parameters required)");
        try {
            t.init((javax.xml.crypto.dsig.spec.TransformParameterSpec) new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(java.util.Collections.<String>emptyList()));
            check(false, "expected InvalidAlgorithmParameterException for mismatched parameter spec");
        } catch (java.security.InvalidAlgorithmParameterException expected) {
            check(true, "init(mismatched spec) -> InvalidAlgorithmParameterException");
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
        check(t instanceof javax.xml.crypto.dsig.TransformService, "DOMBase64Transform is a TransformService");
    }
}
