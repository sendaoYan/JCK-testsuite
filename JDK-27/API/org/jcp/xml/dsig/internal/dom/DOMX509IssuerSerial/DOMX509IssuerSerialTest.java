/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMX509IssuerSerialTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMX509IssuerSerialTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial", false, DOMX509IssuerSerialTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.keyinfo.X509IssuerSerial"), "iface:javax.xml.crypto.dsig.keyinfo.X509IssuerSerial");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getIssuerName"), "method:getIssuerName");
        check(methods.contains("getSerialNumber"), "method:getSerialNumber");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
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
