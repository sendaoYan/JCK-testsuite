/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.X509Data conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys.content:+open
 * @run main X509DataTest
 */
import java.lang.reflect.*;
import java.util.*;
public class X509DataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.X509Data", false, X509DataTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.content.X509Data"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent"), "iface:com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("add"), "method:add");
        check(methods.contains("addCRL"), "method:addCRL");
        check(methods.contains("addCertificate"), "method:addCertificate");
        check(methods.contains("addDigest"), "method:addDigest");
        check(methods.contains("addIssuerSerial"), "method:addIssuerSerial");
        check(methods.contains("addSKI"), "method:addSKI");
        check(methods.contains("addSubjectName"), "method:addSubjectName");
        check(methods.contains("addUnknownElement"), "method:addUnknownElement");
        check(methods.contains("containsCRL"), "method:containsCRL");
        check(methods.contains("containsCertificate"), "method:containsCertificate");
        check(methods.contains("containsDigest"), "method:containsDigest");
        check(methods.contains("containsIssuerSerial"), "method:containsIssuerSerial");
        check(methods.contains("containsSKI"), "method:containsSKI");
        check(methods.contains("containsSubjectName"), "method:containsSubjectName");
        check(methods.contains("containsUnknownElement"), "method:containsUnknownElement");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("itemCRL"), "method:itemCRL");
        check(methods.contains("itemCertificate"), "method:itemCertificate");
        check(methods.contains("itemDigest"), "method:itemDigest");
        check(methods.contains("itemIssuerSerial"), "method:itemIssuerSerial");
        check(methods.contains("itemSKI"), "method:itemSKI");
        check(methods.contains("itemSubjectName"), "method:itemSubjectName");
        check(methods.contains("itemUnknownElement"), "method:itemUnknownElement");
        check(methods.contains("lengthCRL"), "method:lengthCRL");
        check(methods.contains("lengthCertificate"), "method:lengthCertificate");
        check(methods.contains("lengthDigest"), "method:lengthDigest");
        check(methods.contains("lengthIssuerSerial"), "method:lengthIssuerSerial");
        check(methods.contains("lengthSKI"), "method:lengthSKI");
        check(methods.contains("lengthSubjectName"), "method:lengthSubjectName");
        check(methods.contains("lengthUnknownElement"), "method:lengthUnknownElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
