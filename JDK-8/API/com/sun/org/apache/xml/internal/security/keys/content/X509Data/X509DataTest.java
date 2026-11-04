/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.X509Data conformance: class hierarchy and members.
 * @run main X509DataTest
 */
public class X509DataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.X509Data", false, X509DataTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent"), "has interface com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addIssuerSerial"), "method addIssuerSerial");
        check(mn.contains("add"), "method add");
        check(mn.contains("addSKI"), "method addSKI");
        check(mn.contains("addSubjectName"), "method addSubjectName");
        check(mn.contains("addCertificate"), "method addCertificate");
        check(mn.contains("addCRL"), "method addCRL");
        check(mn.contains("addDigest"), "method addDigest");
        check(mn.contains("addUnknownElement"), "method addUnknownElement");
        check(mn.contains("lengthIssuerSerial"), "method lengthIssuerSerial");
        check(mn.contains("lengthSKI"), "method lengthSKI");
        check(mn.contains("lengthSubjectName"), "method lengthSubjectName");
        check(mn.contains("lengthCertificate"), "method lengthCertificate");
        check(mn.contains("lengthCRL"), "method lengthCRL");
        check(mn.contains("lengthDigest"), "method lengthDigest");
        check(mn.contains("lengthUnknownElement"), "method lengthUnknownElement");
        check(mn.contains("itemIssuerSerial"), "method itemIssuerSerial");
        check(mn.contains("itemSKI"), "method itemSKI");
        check(mn.contains("itemSubjectName"), "method itemSubjectName");
        check(mn.contains("itemCertificate"), "method itemCertificate");
        check(mn.contains("itemCRL"), "method itemCRL");
        check(mn.contains("itemDigest"), "method itemDigest");
        check(mn.contains("itemUnknownElement"), "method itemUnknownElement");
        check(mn.contains("containsIssuerSerial"), "method containsIssuerSerial");
        check(mn.contains("containsSKI"), "method containsSKI");
        check(mn.contains("containsSubjectName"), "method containsSubjectName");
        check(mn.contains("containsCertificate"), "method containsCertificate");
        check(mn.contains("containsDigest"), "method containsDigest");
        check(mn.contains("containsCRL"), "method containsCRL");
        check(mn.contains("containsUnknownElement"), "method containsUnknownElement");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
