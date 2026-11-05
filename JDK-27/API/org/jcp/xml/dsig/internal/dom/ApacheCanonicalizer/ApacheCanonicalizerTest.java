/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main ApacheCanonicalizerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ApacheCanonicalizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer", false, ApacheCanonicalizerTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dsig.TransformService"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("getParameterSpec"), "method:getParameterSpec");
        check(methods.contains("init"), "method:init");
        check(methods.contains("isFeatureSupported"), "method:isFeatureSupported");
        check(methods.contains("marshalParams"), "method:marshalParams");
        check(methods.contains("transform"), "method:transform");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("canonicalizer"), "field:canonicalizer");
        check(fields.contains("inclusiveNamespaces"), "field:inclusiveNamespaces");
        check(fields.contains("ownerDoc"), "field:ownerDoc");
        check(fields.contains("params"), "field:params");
        check(fields.contains("transformElem"), "field:transformElem");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer");
        check("javax.xml.crypto.dsig.TransformService".equals(c.getSuperclass().getName()), "superclass javax.xml.crypto.dsig.TransformService");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("canonicalize", "getParameterSpec", "init", "isFeatureSupported", "marshalParams", "transform"))),
                "exact public/protected method face (6 names): " + names);
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
        javax.xml.crypto.dsig.TransformService c14n =
                javax.xml.crypto.dsig.TransformService.getInstance(
                        javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, "DOM");
        check(c14n instanceof org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer,
                "the registered INCLUSIVE c14n service extends ApacheCanonicalizer");
        javax.xml.crypto.dsig.TransformService exc =
                javax.xml.crypto.dsig.TransformService.getInstance(
                        javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS, "DOM");
        check(exc instanceof org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer,
                "the registered exclusive c14n service also extends ApacheCanonicalizer");
        check(!org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer.class.equals(c14n.getClass()),
                "ApacheCanonicalizer itself stays abstract");
    }
}
