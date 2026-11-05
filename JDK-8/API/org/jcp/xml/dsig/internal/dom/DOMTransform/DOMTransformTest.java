/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMTransform conformance: type shape via reflection (headless-safe).
 * @run main DOMTransformTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMTransform", false, DOMTransformTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.Transform"), "implements javax.xml.crypto.dsig.Transform");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getAlgorithm"), "declares getAlgorithm");
        check(hasMethod(c, "getParameterSpec"), "declares getParameterSpec");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "marshal"), "declares marshal");
        check(hasMethod(c, "transform"), "declares transform");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMTransform");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.Transform"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getAlgorithm", "getParameterSpec", "hashCode", "marshal", "transform"))),
                "exact public/protected method face (6 names): " + names);
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
        org.jcp.xml.dsig.internal.dom.DOMTransform dt =
                new org.jcp.xml.dsig.internal.dom.DOMTransform(c14n);
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals(dt.getAlgorithm()),
                "wrapped algorithm forwards");
        check(dt.getParameterSpec() == null, "no parameters for the default c14n");
        check(dt instanceof javax.xml.crypto.dsig.Transform, "DOMTransform is a Transform");
    }
}
