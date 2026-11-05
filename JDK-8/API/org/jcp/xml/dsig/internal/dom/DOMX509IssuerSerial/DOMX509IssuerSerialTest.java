/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial conformance: type shape via reflection (headless-safe).
 * @run main DOMX509IssuerSerialTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMX509IssuerSerialTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial", false, DOMX509IssuerSerialTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.keyinfo.X509IssuerSerial"), "implements javax.xml.crypto.dsig.keyinfo.X509IssuerSerial");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getIssuerName"), "declares getIssuerName");
        check(hasMethod(c, "getSerialNumber"), "declares getSerialNumber");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "marshal"), "declares marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.keyinfo.X509IssuerSerial"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getIssuerName", "getSerialNumber", "hashCode", "marshal"))),
                "exact public/protected method face (5 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.lang.String,java.math.BigInteger,"), "constructor(String, BigInteger) present");
        check(ctorFace.contains("org.w3c.dom.Element,"), "constructor(Element) present");
        // black-box behavior
        org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial is =
                new org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial("CN=Test", java.math.BigInteger.TEN);
        check("CN=Test".equals(is.getIssuerName()), "getIssuerName round trip");
        check(java.math.BigInteger.TEN.equals(is.getSerialNumber()), "getSerialNumber round trip");
        check(is.equals(new org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial("CN=Test", java.math.BigInteger.TEN)),
                "value equals");
        check(!is.equals(new org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial("CN=Other", java.math.BigInteger.TEN)),
                "issuer difference breaks equals");
        check(!is.equals(new org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial("CN=Test", java.math.BigInteger.ONE)),
                "serial difference breaks equals");
        check(is instanceof javax.xml.crypto.dsig.keyinfo.X509IssuerSerial, "is an X509IssuerSerial");
    }
}
