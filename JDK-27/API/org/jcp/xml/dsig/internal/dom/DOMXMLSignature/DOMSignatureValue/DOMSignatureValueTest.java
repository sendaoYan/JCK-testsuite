/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignature.DOMSignatureValue conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMSignatureValueTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMSignatureValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignature$DOMSignatureValue", false, DOMSignatureValueTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMXMLSignature$DOMSignatureValue"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.XMLSignature$SignatureValue"), "iface:javax.xml.crypto.dsig.XMLSignature$SignatureValue");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getEncodedValue"), "method:getEncodedValue");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getValue"), "method:getValue");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        check(methods.contains("validate"), "method:validate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
