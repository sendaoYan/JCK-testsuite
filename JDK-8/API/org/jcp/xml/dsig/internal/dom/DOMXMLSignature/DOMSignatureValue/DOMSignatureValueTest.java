/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignature.DOMSignatureValue conformance: type shape via reflection (headless-safe).
 * @run main DOMSignatureValueTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMSignatureValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignature$DOMSignatureValue", false, DOMSignatureValueTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.XMLSignature$SignatureValue"), "implements javax.xml.crypto.dsig.XMLSignature$SignatureValue");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getEncodedValue"), "declares getEncodedValue");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "marshal"), "declares marshal");
        check(hasMethod(c, "validate"), "declares validate");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: live SignatureValue contract ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignature$DOMSignatureValue");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()),
                "superclass DOMStructure");
        check(javax.xml.crypto.dsig.XMLSignature.SignatureValue.class.isAssignableFrom(c),
                "implements XMLSignature.SignatureValue");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !java.lang.reflect.Modifier.isStatic(c.getModifiers()),
                "public non-static nested class bound to the enclosing signature (constructors package-private)");
        check(c.getDeclaredConstructors().length == 2, "exactly 2 declared constructors");
        // black-box behavior
        javax.xml.crypto.dsig.XMLSignatureFactory xsf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.DigestMethod dm = xsf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.CanonicalizationMethod cm = xsf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null);
        javax.xml.crypto.dsig.SignatureMethod sm = xsf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null);
        javax.xml.crypto.dsig.Reference ref = xsf.newReference("#o1", dm);
        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kif =
                javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance("DOM");
        javax.xml.crypto.dsig.keyinfo.KeyInfo ki =
                kif.newKeyInfo(java.util.Arrays.asList(kif.newKeyName("kk")), "ki");
        javax.xml.crypto.dsig.SignedInfo si = xsf.newSignedInfo(cm, sm, java.util.Arrays.asList(ref), "si1");
        org.jcp.xml.dsig.internal.dom.DOMXMLSignature sig =
                new org.jcp.xml.dsig.internal.dom.DOMXMLSignature(si, ki, null, null, "sv-id");
        javax.xml.crypto.dsig.XMLSignature.SignatureValue sv = sig.getSignatureValue();
        check("sv-id".equals(sv.getId()), "signature value id round trip");
        check(sv.getValue() == null, "value null before signing");
        check(sv instanceof org.jcp.xml.dsig.internal.dom.DOMXMLSignature.DOMSignatureValue,
                "implementation type is DOMSignatureValue");
        check(sv instanceof javax.xml.crypto.dsig.XMLSignature.SignatureValue, "is a SignatureValue");
        org.jcp.xml.dsig.internal.dom.DOMXMLSignature sig2 =
                new org.jcp.xml.dsig.internal.dom.DOMXMLSignature(si, null, null, "s2", null);
        check(sig2.getSignatureValue().getId() == null, "fifth constructor argument drives the value id");
    }
}
