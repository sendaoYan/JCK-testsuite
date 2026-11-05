/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMCryptoBinary conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMCryptoBinaryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMCryptoBinaryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMCryptoBinary", false, DOMCryptoBinaryTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMCryptoBinary"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getBigNum"), "method:getBigNum");
        check(methods.contains("marshal"), "method:marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMCryptoBinary");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getBigNum", "marshal"))),
                "exact public/protected method face (2 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.math.BigInteger,"), "constructor(BigInteger) present");
        check(ctorFace.contains("org.w3c.dom.Node,"), "constructor(Node) present");
        // black-box behavior
        org.jcp.xml.dsig.internal.dom.DOMCryptoBinary cb =
                new org.jcp.xml.dsig.internal.dom.DOMCryptoBinary(java.math.BigInteger.valueOf(12345));
        check(java.math.BigInteger.valueOf(12345).equals(cb.getBigNum()), "getBigNum round trip");
        check(!cb.equals(new org.jcp.xml.dsig.internal.dom.DOMCryptoBinary(java.math.BigInteger.valueOf(12345))),
                "equality requires the marshalled node (fresh instances are unequal)");
        org.jcp.xml.dsig.internal.dom.DOMCryptoBinary cb2 =
                new org.jcp.xml.dsig.internal.dom.DOMCryptoBinary(java.math.BigInteger.ZERO);
        check(java.math.BigInteger.ZERO.equals(cb2.getBigNum()), "zero value round trip");
        check(cb != cb2, "distinct values are distinct objects");
    }
}
