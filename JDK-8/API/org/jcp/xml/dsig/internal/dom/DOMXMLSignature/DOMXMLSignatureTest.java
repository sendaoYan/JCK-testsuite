/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignature conformance: type shape via reflection (headless-safe).
 * @run main DOMXMLSignatureTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMXMLSignatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignature", false, DOMXMLSignatureTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.XMLSignature"), "implements javax.xml.crypto.dsig.XMLSignature");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getKeyInfo"), "declares getKeyInfo");
        check(hasMethod(c, "getKeySelectorResult"), "declares getKeySelectorResult");
        check(hasMethod(c, "getObjects"), "declares getObjects");
        check(hasMethod(c, "getSignatureValue"), "declares getSignatureValue");
        check(hasMethod(c, "getSignedInfo"), "declares getSignedInfo");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignature");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.XMLSignature"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getId", "getKeyInfo", "getKeySelectorResult", "getObjects", "getSignatureValue", "getSignedInfo", "hashCode", "marshal", "sign", "validate"))),
                "exact public/protected method face (11 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("javax.xml.crypto.dsig.SignedInfo,javax.xml.crypto.dsig.keyinfo.KeyInfo,java.util.List,java.lang.String,java.lang.String,"), "constructor(SignedInfo, KeyInfo, List, String, String) present");
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
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
        javax.xml.crypto.dsig.XMLObject obj =
                xsf.newXMLObject(java.util.Collections.<javax.xml.crypto.XMLStructure>emptyList(), "o1", null, null);
        org.jcp.xml.dsig.internal.dom.DOMXMLSignature sig1 =
                new org.jcp.xml.dsig.internal.dom.DOMXMLSignature(si, ki,
                        java.util.Arrays.<javax.xml.crypto.dsig.XMLObject>asList(obj), null, "sv-id");
        check(sig1.getId() == null, "no signature id supplied");
        check(sig1.getKeyInfo() == ki, "key info forwarded");
        check(sig1.getSignedInfo() == si, "signed info forwarded");
        check(sig1.getObjects().size() == 1, "one object retained");
        check("sv-id".equals(sig1.getSignatureValue().getId()), "signature value id round trip");
        check(sig1.getSignatureValue().getValue() == null, "signature value empty before signing");
        try {
            sig1.getObjects().add((javax.xml.crypto.dsig.XMLObject) null);
            check(false, "expected UOE modifying objects");
        } catch (UnsupportedOperationException expected) {
            check(true, "objects list is unmodifiable");
        }
        org.jcp.xml.dsig.internal.dom.DOMXMLSignature sig2 =
                new org.jcp.xml.dsig.internal.dom.DOMXMLSignature(si, null, null, "sig-id", null);
        check("sig-id".equals(sig2.getId()), "signature id round trip");
        check(sig2.getKeyInfo() == null, "null key info stays null");
        check(sig2.getObjects().isEmpty(), "null objects become empty");
        check(sig1 instanceof javax.xml.crypto.dsig.XMLSignature, "DOMXMLSignature is an XMLSignature");
    }
}
