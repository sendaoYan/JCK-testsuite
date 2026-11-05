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
