/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMSignedInfo conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMSignedInfoTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMSignedInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignedInfo", false, DOMSignedInfoTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMSignedInfo"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.SignedInfo"), "iface:javax.xml.crypto.dsig.SignedInfo");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getCanonicalizationMethod"), "method:getCanonicalizationMethod");
        check(methods.contains("getCanonicalizedData"), "method:getCanonicalizedData");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getReferences"), "method:getReferences");
        check(methods.contains("getSignatureMethod"), "method:getSignatureMethod");
        check(methods.contains("getSignedInfoReferences"), "method:getSignedInfoReferences");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
